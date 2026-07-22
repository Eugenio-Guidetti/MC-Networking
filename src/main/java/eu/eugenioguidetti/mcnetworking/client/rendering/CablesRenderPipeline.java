package eu.eugenioguidetti.mcnetworking.client.rendering;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/05/2026
 */

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Eugenio Guidetti
 */
public class CablesRenderPipeline implements ClientModInitializer
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
    // 26.2: VertexFormat#uploadImmediateIndexBuffer è stato rimosso, quindi ora gestiamo
    // anche il buffer degli indici (ordinati per la trasparenza) manualmente, con lo stesso
    // meccanismo di ring buffer già usato per i vertici.
    private MappableRingBuffer indexBuffer;

    // :::custom-pipelines:drawing-phase
    public static CablesRenderPipeline getInstance()
    {
        return instance;
    }

    public static void addCable(@NotNull BlockPos startPos,
                                @NotNull Direction startFace,
                                @NotNull BlockPos targetPos,
                                @NotNull Direction targetFace,
                                @NotNull CableType type)
    {
        // Generiamo la chiave usando le coordinate e la faccia di partenza
        CableKey key = new CableKey(startPos, startFace);

        if (activeCables.containsKey(key))
        {
            return;
        }

        // Calcoliamo i punti reali 3D del cavo
        Vec3 pointA = Utils.getInterfaceCenterPoint(startPos, startFace);
        Vec3 pointB = Utils.getInterfaceCenterPoint(targetPos, targetFace);

        // Estraiamo il colore dal tipo di cavo
        float r = type.getRed() / 255f;
        float g = type.getGreen() / 255f;
        float b = type.getBlue() / 255f;
        float a = 1;
        float w = type.lineWidth();

        // Ordina i due punti: indipendentemente da chi chiama addCable,
        // la coppia di punti sarà sempre nello stesso ordine.
        boolean swap = startPos.compareTo(targetPos) > 0;
        Vec3 renderA = swap ? pointB : pointA;
        Vec3 renderB = swap ? pointA : pointB;

        CableRenderState state = new CableRenderState(renderA, renderB, r, g, b, a, w);

        // Inseriamo o aggiorniamo il cavo nella mappa
        activeCables.put(key, state);
    }

    public static void removeCable(@NotNull BlockPos pos, @NotNull Direction face)
    {
        CableKey key = new CableKey(pos, face);
        CableRenderState state = activeCables.get(key);
        activeCables.values().removeIf(value -> value.equals(state));
    }

    public static void removeCablesFromBlock(@NotNull BlockPos pos)
    {
        activeCables.keySet().removeIf(key -> key.pos().equals(pos));
    }

    public static void clearCables()
    {
        activeCables.clear();
    }

    private void draw(Minecraft client,
                      RenderPipeline pipeline,
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
            // In 26.2 pipeline.getVertexFormat().uploadImmediateIndexBuffer(...) non esiste più:
            // carichiamo noi l'indice ordinato in un GpuBuffer dedicato (vedi uploadIndices sotto).
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
                                  Optional.<Vector4fc>empty(),
                                  client.gameRenderer.mainRenderTarget().getDepthTextureView(),
                                  OptionalDouble.empty()))
        {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            // setVertexBuffer ora vuole una GpuBufferSlice, non più una GpuBuffer "grezza"
            renderPass.setVertexBuffer(0, vertices.slice(0, vertexBufferSize));
            renderPass.setIndexBuffer(indices, indexType);

            // drawIndexed in 26.2 ha un nuovo ordine dei parametri:
            // (indexCount, instanceCount, firstIndex, baseVertex, firstInstance).
            // baseVertex resta 0 perché i vertici vengono sempre caricati dall'inizio della slice.
            renderPass.drawIndexed(drawParameters.indexCount(), 1, 0, 0, 0);
        }

        builtBuffer.close();
    }

    @Override
    public void onInitializeClient()
    {
        instance = this;

        clearCables();

        LevelRenderEvents.END_EXTRACTION.register(this::extractCables);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::renderAndDrawCables);
    }

    private void extractCables(LevelExtractionContext context)
    {
        extractedCableStates.clear();
        extractedCableStates.addAll(activeCables.values());
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

            // 1. Calcoliamo la DIREZIONE in cui viaggia il cavo
            Vector3f dir = new Vector3f(pB).sub(pA).normalize();

            // 2. Troviamo due vettori perpendicolari alla direzione per creare lo spessore
            Vector3f up = new Vector3f(0, 1, 0);
            // Prevenzione del bug matematico se il cavo va perfettamente dritto verso l'alto
            if (Math.abs(dir.y) > 0.99f)
            {
                up.set(1, 0, 0);
            }

            Vector3f right = new Vector3f(dir).cross(up).normalize().mul(cable.lineWidthMult);
            up = new Vector3f(right).cross(dir).normalize().mul(cable.lineWidthMult); // Ricalcoliamo l'up per renderlo perfetto

            // 3. Calcoliamo i 4 angoli attorno al punto centrale del cavo
            Vector3f[] offsets = new Vector3f[]{new Vector3f(right).add(up),       // In alto a destra
                    new Vector3f(right).sub(up),       // In basso a destra
                    new Vector3f(right).negate().sub(up), // In basso a sinistra
                    new Vector3f(right).negate().add(up)  // In alto a sinistra
            };

            // 4. Disegniamo le 4 facce (le "pareti" laterali del tubo 3D)
            for (int i = 0; i < 4; i++)
            {
                int next = (i + 1) % 4;

                // Creiamo i 4 vertici per la singola faccia
                Vector3f v1 = new Vector3f(pA).add(offsets[i]);
                Vector3f v2 = new Vector3f(pA).add(offsets[next]);
                Vector3f v3 = new Vector3f(pB).add(offsets[next]);
                Vector3f v4 = new Vector3f(pB).add(offsets[i]);

                // Calcoliamo la normale per l'illuminazione
                Vector3f normal = new Vector3f(offsets[i]).add(offsets[next]).normalize();

                // Diamo i vertici in senso antiorario al buffer
                this.buffer
                        .addVertex(positionMatrix, v1.x, v1.y, v1.z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normal.x, normal.y, normal.z);
                this.buffer
                        .addVertex(positionMatrix, v2.x, v2.y, v2.z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normal.x, normal.y, normal.z);
                this.buffer
                        .addVertex(positionMatrix, v3.x, v3.y, v3.z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normal.x, normal.y, normal.z);
                this.buffer
                        .addVertex(positionMatrix, v4.x, v4.y, v4.z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normal.x, normal.y, normal.z);

                // 5. DISEGNIAMO IL TAPPO DI PARTENZA (Cap A)
                // La faccia punta esattamente nella direzione opposta a cui va il cavo
                Vector3f normalA = new Vector3f(dir).negate();

                // Per pA invertiamo l'ordine dei vertici (0, 3, 2, 1) per renderlo visibile dall'esterno
                this.buffer
                        .addVertex(positionMatrix, pA.x + offsets[0].x, pA.y + offsets[0].y, pA.z + offsets[0].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalA.x, normalA.y, normalA.z);
                this.buffer
                        .addVertex(positionMatrix, pA.x + offsets[3].x, pA.y + offsets[3].y, pA.z + offsets[3].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalA.x, normalA.y, normalA.z);
                this.buffer
                        .addVertex(positionMatrix, pA.x + offsets[2].x, pA.y + offsets[2].y, pA.z + offsets[2].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalA.x, normalA.y, normalA.z);
                this.buffer
                        .addVertex(positionMatrix, pA.x + offsets[1].x, pA.y + offsets[1].y, pA.z + offsets[1].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalA.x, normalA.y, normalA.z);

                // 6. DISEGNIAMO IL TAPPO DI ARRIVO (Cap B)
                // La faccia punta esattamente nella stessa direzione del cavo
                Vector3f normalB = new Vector3f(dir);

                // Per pB usiamo l'ordine standard (0, 1, 2, 3)
                this.buffer
                        .addVertex(positionMatrix, pB.x + offsets[0].x, pB.y + offsets[0].y, pB.z + offsets[0].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalB.x, normalB.y, normalB.z);
                this.buffer
                        .addVertex(positionMatrix, pB.x + offsets[1].x, pB.y + offsets[1].y, pB.z + offsets[1].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalB.x, normalB.y, normalB.z);
                this.buffer
                        .addVertex(positionMatrix, pB.x + offsets[2].x, pB.y + offsets[2].y, pB.z + offsets[2].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalB.x, normalB.y, normalB.z);
                this.buffer
                        .addVertex(positionMatrix, pB.x + offsets[3].x, pB.y + offsets[3].y, pB.z + offsets[3].z)
                        .setColor(r, g, b, a)
                        .setNormal(matrices.last(), normalB.x, normalB.y, normalB.z);
            }
        }

        matrices.popPose();
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

    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer)
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

        // 26.2: CommandEncoder#mapBuffer è stato rimosso, la mappatura si chiama ora sulla slice stessa
        try (GpuBufferSlice.MappedView mappedView = this.vertexBuffer
                .currentBuffer()
                .slice(0, builtBuffer.vertexBuffer().remaining())
                .map(false, true))
        {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }

    // Nuovo metodo richiesto dalla 26.2: prima questo lavoro lo faceva
    // pipeline.getVertexFormat().uploadImmediateIndexBuffer(...), rimosso in questa versione.
    // Carichiamo quindi a mano l'indice (già ordinato per la trasparenza da sortQuads) in un
    // ring buffer dedicato, con lo stesso identico procedimento usato sopra per i vertici.
    private GpuBuffer uploadIndices(ByteBuffer indexData)
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
    public record CableKey(BlockPos pos, Direction face)
    {
    }
}