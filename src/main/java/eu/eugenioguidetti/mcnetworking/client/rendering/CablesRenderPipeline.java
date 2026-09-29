package eu.eugenioguidetti.mcnetworking.client.rendering;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/05/2026
 */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.gui.ClientCommandHistoryCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.CABLES_TENSION;

/**
 *
 * @author Eugenio Guidetti
 */
public class CablesRenderPipeline
{
    private static final RenderPipeline CABLES_PIPELINE = RenderPipelines.register(RenderPipeline
                                                                                           .builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                                                                                           .withLocation(Identifier.fromNamespaceAndPath(
                                                                                                   MCNetworking.MOD_ID,
                                                                                                   "pipeline/cables_solid"))
                                                                                           .build());
    private static final Map<CableKey, CableRenderState> activeCables = new ConcurrentHashMap<>();
    private static final List<CableRenderState> extractedCableStates = new ArrayList<>();
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static CablesRenderPipeline instance;
    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;
    private MappableRingBuffer indexBuffer;


    public CablesRenderPipeline()
    {
        instance = this;

        clearCables();

        LevelRenderEvents.END_EXTRACTION.register(this::extractCables);
        LevelRenderEvents.END_MAIN.register(this::renderAndDrawCables);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                                                       {
                                                           CablesRenderPipeline.clearCables();
                                                           TerminalCache.clearAll();
                                                           ClientCommandHistoryCache.clearAll();
                                                       });

        InvalidateRenderStateCallback.EVENT.register(() ->
                                                     {
                                                         // Svuota la mappa dei cavi attivi quando si preme F3 + A
                                                         CablesRenderPipeline.clearCables();

                                                         // Forza la chiusura e la rigenerazione dei buffer se aperti
                                                         if (this.buffer != null)
                                                         {
                                                             this.buffer = null;
                                                         }
                                                     });
    }


    // :::custom-pipelines:drawing-phase
    public static CablesRenderPipeline getInstance()
    {
        return instance;
    }


    public static void addCable(@NotNull GlobalPos startPos,
                                @NotNull Direction startFace,
                                @NotNull GlobalPos targetPos,
                                @NotNull Direction targetFace,
                                @NotNull CableType type)
    {
        if (!startPos.dimension().equals(targetPos.dimension()))
        {
            throw new IllegalStateException("Non puoi aggiungere un cavo tra dimensioni diverse");
        }

        // Generiamo la chiave usando le coordinate e la faccia di partenza
        CableKey key = new CableKey(startPos, startFace);

        if (activeCables.containsKey(key))
        {
            return;
        }

        // Calcoliamo i punti reali 3D del cavo
        Vec3 pointA = Utils.getInterfaceCenterPoint(startPos.pos(), startFace);
        Vec3 pointB = Utils.getInterfaceCenterPoint(targetPos.pos(), targetFace);

        // Estraiamo il colore dal tipo di cavo
        float r = type.getRed() / 255f;
        float g = type.getGreen() / 255f;
        float b = type.getBlue() / 255f;
        float a = 1;
        float w = type.lineWidth();

        // Ordina i due punti: indipendentemente da chi chiama addCable,
        // la coppia di punti sarà sempre nello stesso ordine.
        boolean swap = startPos.pos().compareTo(targetPos.pos()) > 0;
        Vec3 renderA = swap ? pointB : pointA;
        Vec3 renderB = swap ? pointA : pointB;

        CableRenderState state = new CableRenderState(renderA, renderB, r, g, b, a, w);

        // Inseriamo o aggiorniamo il cavo nella mappa
        activeCables.put(key, state);
    }

    public static void removeCable(@NotNull GlobalPos pos, @NotNull Direction face)
    {
        CableKey key = new CableKey(pos, face);
        CableRenderState state = activeCables.get(key);
        activeCables.values().removeIf(value -> value.equals(state));
    }

    public static void removeCablesFromBlock(@NotNull GlobalPos pos)
    {
        activeCables.keySet().removeIf(key -> key.pos().equals(pos));
    }

    public static void clearCables()
    {
        activeCables.clear();
    }

    private void draw(Minecraft client,
                      @NonNull RenderPipeline pipeline,
                      MeshData builtBuffer,
                      MeshData.DrawState drawParameters,
                      GpuBuffer vertices,
                      VertexFormat format)
    {
        GpuBuffer indices;
        IndexType indexType;

        if (pipeline.getPrimitiveTopology() == PrimitiveTopology.QUADS)
        {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = this.uploadIndices(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        }
        else
        {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getPrimitiveTopology());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem
                .getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem
                .getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> MCNetworking.MOD_ID + " cables render pipeline rendering",
                                  client.gameRenderer.mainRenderTarget().getColorTextureView(),
                                  Optional.empty(),
                                  client.gameRenderer.mainRenderTarget().getDepthTextureView(),
                                  OptionalDouble.empty()))
        {
            // Recupera la pipeline compilata dalla cache di RenderSystem
            CompiledRenderPipeline compiledPipeline = RenderSystem.getCompiledPipeline(pipeline);
            renderPass.setPipeline(compiledPipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            // setVertexBuffer ora vuole una GpuBufferSlice, non più una GpuBuffer "grezza"
            renderPass.setVertexBuffer(0, vertices.slice(0, vertexBufferSize));
            renderPass.setIndexBuffer(indices, indexType);

            renderPass.drawIndexed(drawParameters.indexCount(), 1, 0, 0, 0);
        }

        builtBuffer.close();
    }

    private void extractCables(LevelExtractionContext context)
    {
        extractedCableStates.clear();
        for (var entry : activeCables.entrySet())
        {
            if (!entry.getKey().pos().dimension().equals(context.level().dimension()))
            {
                continue;
            }

            extractedCableStates.add(entry.getValue());
        }
    }

    // :::custom-pipelines:drawing-phase
    private void renderAndDrawCables(LevelRenderContext context)
    {
        this.renderCables(context);
        if (this.buffer != null)
        {
            this.executeDrawCall(Minecraft.getInstance(), CABLES_PIPELINE);
        }
    }

    private void renderCables(LevelRenderContext context)
    {
        if (extractedCableStates.isEmpty())
        {
            return;
        }

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (this.buffer == null)
        {
            this.buffer = new BufferBuilder(ALLOCATOR, PrimitiveTopology.QUADS, CABLES_PIPELINE.getVertexFormatBinding(0));
        }

        Matrix4fc positionMatrix = matrices.last().pose();

        for (CableRenderState cable : extractedCableStates)
        {
            // Colori formattati 0-255
            int r = (int) (cable.r * 255);
            int g = (int) (cable.g * 255);
            int b = (int) (cable.b * 255);
            int a = (int) (cable.a * 255);

            // Vettori di partenza e arrivo
            Vector3f pA = new Vector3f((float) cable.posA.x, (float) cable.posA.y, (float) cable.posA.z);
            Vector3f pB = new Vector3f((float) cable.posB.x, (float) cable.posB.y, (float) cable.posB.z);

            float distance = pA.distance(pB);
            int segments = Math.clamp((int) (distance * 2), 16, 64);
            float sag = distance * CABLES_TENSION;

            Vector3f prevPoint = new Vector3f(pA);

            for (int s = 1; s <= segments; s++)
            {
                float t = (float) s / segments;
                float currentSag = sag * 4 * t * (1 - t);
                Vector3f currentPoint = new Vector3f(pA).lerp(pB, t).sub(0, currentSag, 0);

                Vector3f dir = new Vector3f(currentPoint).sub(prevPoint).normalize();
                Vector3f overlapOffset = new Vector3f(dir).mul(0.02f);
                Vector3f renderPrev = new Vector3f(prevPoint).sub(overlapOffset);
                Vector3f renderCurrent = new Vector3f(currentPoint).add(overlapOffset);

                Vector3f up = new Vector3f(0, 1, 0);
                if (Math.abs(dir.y) > 0.99f)
                {
                    up.set(1, 0, 0);
                }

                Vector3f right = new Vector3f(dir).cross(up).normalize().mul(cable.lineWidthMult);
                up = new Vector3f(right).cross(dir).normalize().mul(cable.lineWidthMult);

                Vector3f normalUp = new Vector3f(up).normalize();
                Vector3f normalRight = new Vector3f(right).normalize();

                // Faccia Verticale
                addQuad(positionMatrix,
                        matrices,
                        r,
                        g,
                        b,
                        a,
                        new Vector3f(renderPrev).add(up),
                        new Vector3f(renderPrev).sub(up),
                        new Vector3f(renderCurrent).sub(up),
                        new Vector3f(renderCurrent).add(up),
                        normalRight);

                addQuad(positionMatrix,
                        matrices,
                        r,
                        g,
                        b,
                        a,
                        new Vector3f(renderPrev).add(up),
                        new Vector3f(renderCurrent).add(up),
                        new Vector3f(renderCurrent).sub(up),
                        new Vector3f(renderPrev).sub(up),
                        new Vector3f(normalRight).negate());

                // Faccia Orizzontale
                addQuad(positionMatrix,
                        matrices,
                        r,
                        g,
                        b,
                        a,
                        new Vector3f(renderPrev).add(right),
                        new Vector3f(renderPrev).sub(right),
                        new Vector3f(renderCurrent).sub(right),
                        new Vector3f(renderCurrent).add(right),
                        normalUp);

                addQuad(positionMatrix,
                        matrices,
                        r,
                        g,
                        b,
                        a,
                        new Vector3f(renderPrev).add(right),
                        new Vector3f(renderCurrent).add(right),
                        new Vector3f(renderCurrent).sub(right),
                        new Vector3f(renderPrev).sub(right),
                        new Vector3f(normalUp).negate());

                prevPoint = currentPoint;
            }
        }

        matrices.popPose();
    }

    private void addQuad(Matrix4fc positionMatrix,
                         @NonNull PoseStack matrices,
                         int r,
                         int g,
                         int b,
                         int a,
                         @NonNull Vector3f v1,
                         @NonNull Vector3f v2,
                         @NonNull Vector3f v3,
                         @NonNull Vector3f v4,
                         @NonNull Vector3f normal)
    {
        PoseStack.Pose last = matrices.last();
        this.buffer.addVertex(positionMatrix, v1.x, v1.y, v1.z).setColor(r, g, b, a).setNormal(last, normal.x, normal.y, normal.z);
        this.buffer.addVertex(positionMatrix, v2.x, v2.y, v2.z).setColor(r, g, b, a).setNormal(last, normal.x, normal.y, normal.z);
        this.buffer.addVertex(positionMatrix, v3.x, v3.y, v3.z).setColor(r, g, b, a).setNormal(last, normal.x, normal.y, normal.z);
        this.buffer.addVertex(positionMatrix, v4.x, v4.y, v4.z).setColor(r, g, b, a).setNormal(last, normal.x, normal.y, normal.z);
    }

    private void executeDrawCall(Minecraft client, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline)
    {
        // Build the buffer
        MeshData builtBuffer = this.buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = this.upload(drawParameters, format, builtBuffer);

        this.draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        // Rotate the vertex/index buffers so we are less likely to use buffers that the GPU is using
        this.vertexBuffer.rotate();
        if (this.indexBuffer != null)
        {
            this.indexBuffer.rotate();
        }
        this.buffer = null;
    }

    private @NonNull GpuBuffer upload(MeshData.@NonNull DrawState drawParameters, @NonNull VertexFormat format, MeshData builtBuffer)
    {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (this.vertexBuffer == null || this.vertexBuffer.size() < vertexBufferSize)
        {
            if (this.vertexBuffer != null)
            {
                this.vertexBuffer.close();
            }

            this.vertexBuffer = new MappableRingBuffer(() -> MCNetworking.MOD_ID + " cables render pipeline",
                                                       GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                                                       vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBufferSlice.MappedView mappedView = this.vertexBuffer
                .currentBuffer()
                .slice(0, builtBuffer.vertexBuffer().remaining())
                .map(false, true))
        {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }

    private @NonNull GpuBuffer uploadIndices(@NonNull ByteBuffer indexData)
    {
        int indexBufferSize = indexData.remaining();

        if (this.indexBuffer == null || this.indexBuffer.size() < indexBufferSize)
        {
            if (this.indexBuffer != null)
            {
                this.indexBuffer.close();
            }

            this.indexBuffer = new MappableRingBuffer(() -> MCNetworking.MOD_ID + " cables index buffer",
                                                      GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_MAP_WRITE,
                                                      indexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBufferSlice.MappedView mappedView = this.indexBuffer.currentBuffer().slice(0, indexData.remaining()).map(false, true))
        {
            MemoryUtil.memCopy(indexData, mappedView.data());
        }

        return this.indexBuffer.currentBuffer();
    }

    public void close()
    {
        ALLOCATOR.close();

        if (this.vertexBuffer != null)
        {
            this.vertexBuffer.close();
            this.vertexBuffer = null;
        }

        if (this.indexBuffer != null)
        {
            this.indexBuffer.close();
            this.indexBuffer = null;
        }
    }

    // Il record thread-safe da passare alla Drawing Phase
    public record CableRenderState(Vec3 posA, Vec3 posB, float r, float g, float b, float a, float lineWidthMult)
    {
    }

    // Un cavo è identificato univocamente dalla porta (Blocco + Faccia) da cui parte.
    public record CableKey(GlobalPos pos, Direction face)
    {
    }
}