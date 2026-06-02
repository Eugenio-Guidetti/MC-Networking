package eu.eugenioguidetti.mcnetworking.client.rendering;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/05/2026
 */

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.item.CableType;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
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
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Eugenio Guidetti
 */
public class CablesRenderPipeline implements ClientModInitializer
{
    private static CablesRenderPipeline instance;

    private static final RenderPipeline CABLES_PIPELINE = RenderPipelines.register(RenderPipeline
                                                                                           .builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                                                                                           .withLocation(Identifier.fromNamespaceAndPath(
                                                                                                   MCNetworking.MOD_ID,
                                                                                                   "pipeline/cables_solid"))
                                                                                           .build());

    // Mappa il MacAddress (A o B) al record del cavo. Questo ci permette la rimozione rapida (O(1)).
    private static final Map<MacAddress, CableRenderState> activeCables = new ConcurrentHashMap<>();
    private static final List<CableRenderState> extractedCableStates = new ArrayList<>();

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    // :::custom-pipelines:drawing-phase
    public static CablesRenderPipeline getInstance()
    {
        return instance;
    }

    @Override
    public void onInitializeClient()
    {
        instance = this;

        clearCables();
        //activeWaypoints.add(new BlockPos(0, 1, 0));

        LevelRenderEvents.END_EXTRACTION.register(this::extractCables);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::renderAndDrawCables);
    }

    // Il record thread-safe da passare alla Drawing Phase
    public record CableRenderState(Vec3 posA, Vec3 posB, float r, float g, float b, float a, float lineWidthMult)
    {
    }

    public static void addCable(MacAddress macA,
                                BlockPos blockA,
                                Direction faceA,
                                MacAddress macB,
                                BlockPos blockB,
                                Direction faceB,
                                CableType type)
    {
        BlockPos first;
        BlockPos second;
        Direction firstFace;
        Direction secondFace;

        // Imponiamo l'ordine: First > Second
        if (blockA.compareTo(blockB) > 0)
        {
            first = blockA;
            firstFace = faceA;
            second = blockB;
            secondFace = faceB;
        }
        else
        {
            first = blockB;
            firstFace = faceB;
            second = blockA;
            secondFace = faceA;
        }

        // Calcoliamo i centri esatti delle facce dove il cavo si innesterà
        Vec3 vecA = new Vec3(first.getX() + 0.5 + (firstFace.getStepX() * 0.5),
                             first.getY() + 0.5 + (firstFace.getStepY() * 0.5),
                             first.getZ() + 0.5 + (firstFace.getStepZ() * 0.5));

        Vec3 vecB = new Vec3(second.getX() + 0.5 + (secondFace.getStepX() * 0.5),
                             second.getY() + 0.5 + (secondFace.getStepY() * 0.5),
                             second.getZ() + 0.5 + (secondFace.getStepZ() * 0.5));

        //vecA = first.getCenter();
        //vecB = second.getCenter();

        // Estraiamo i colori (presupponendo i metodi nel tuo record CableType)
        float r = type.getRed() / 255f;
        float g = type.getGreen() / 255f;
        float b = type.getBlue() / 255f;

        CableRenderState state = new CableRenderState(vecA, vecB, r, g, b, 1.0f, type.lineWidth());

        // Mappiamo ENTRAMBE le coordinate allo stesso oggetto cavo.
        // Così, se distruggiamo l'Host in 'blockA' O in 'blockB', troveremo il cavo!
        activeCables.put(macA, state);
        activeCables.put(macB, state);
    }

    public static void removeCable(MacAddress mac)
    {
        // Troviamo il cavo associato a questo blocco
        CableRenderState state = activeCables.get(mac);
        if (state != null)
        {
            // Se troviamo il cavo, lo eliminiamo dalla mappa cercando e pulendo tutte le entry
            // che puntano a lui (così ripuliamo anche l'altra estremità).
            activeCables.values().removeIf(val -> val.equals(state));
        }
    }

    public static void clearCables()
    {
        activeCables.clear();
    }

    private void extractCables(LevelExtractionContext context)
    {
        extractedCableStates.clear();
        // Usiamo un Set temporaneo per non estrarre due volte lo stesso cavo
        // (visto che lo avevamo mappato 2 volte nell'HashMap)
        Set<CableRenderState> uniqueCables = new HashSet<>(activeCables.values());
        extractedCableStates.addAll(uniqueCables);
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

        // ATTENZIONE: Qui ora usiamo VertexFormat.Mode.QUADS! Non più LINES!
        if (this.buffer == null)
        {
            this.buffer = new BufferBuilder(ALLOCATOR, VertexFormat.Mode.QUADS, CABLES_PIPELINE.getVertexFormat());
        }

        Matrix4fc positionMatrix = matrices.last().pose();

        // LO SPESSORE REALE DEL CAVO IN BLOCCHI (es. 0.05 = 5% di un blocco)
        // Modifica questo valore per rendere il cavo più o meno cicciotto!

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

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        // Rotate the vertex buffer so we are less likely to use buffers that the GPU is using
        this.vertexBuffer.rotate();
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

            this.vertexBuffer = new MappableRingBuffer(() -> MCNetworking.MOD_ID + " example render pipeline",
                                                       GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                                                       vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer
                                                                                .currentBuffer()
                                                                                .slice(0, builtBuffer.vertexBuffer().remaining()),
                                                                        false,
                                                                        true))
        {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client,
                             RenderPipeline pipeline,
                             MeshData builtBuffer,
                             MeshData.DrawState drawParameters,
                             GpuBuffer vertices,
                             VertexFormat format)
    {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS)
        {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            // Upload the index buffer
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        }
        else
        {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem
                .getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem
                .getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> MCNetworking.MOD_ID + " example render pipeline rendering",
                                  client.getMainRenderTarget().getColorTextureView(),
                                  OptionalInt.empty(),
                                  client.getMainRenderTarget().getDepthTextureView(),
                                  OptionalDouble.empty()))
        {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close()
    {
        ALLOCATOR.close();

        if (this.vertexBuffer != null)
        {
            this.vertexBuffer.close();
            this.vertexBuffer = null;
        }
    }
}
