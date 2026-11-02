package eu.eugenioguidetti.mcnetworking.webui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/10/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.networking.packet.WebuiCliHistoryC2SPayload;
import eu.eugenioguidetti.mcnetworking.networking.packet.WebuiCliHistoryS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Eugenio Guidetti
 */
public class ClientWorldScanner
{
    public record DeviceSummary(String hostname, String tipo, BlockPos pos)
    {
    }

    private static final Map<BlockPos, CompletableFuture<List<String>>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    public static @NonNull CompletableFuture<Map<BlockPos, DeviceSummary>> getLoadedDevicesFuture()
    {
        Minecraft client = Minecraft.getInstance();
        CompletableFuture<Map<BlockPos, DeviceSummary>> queryFuture = new CompletableFuture<>();

        client.execute(() ->
                       {
                           try
                           {
                               ClientLevel level = client.level;
                               if (level == null || client.player == null)
                               {
                                   queryFuture.complete(Map.of());
                                   return;
                               }

                               ClientChunkCache chunkSource = level.getChunkSource();
                               int renderDistance = client.options.renderDistance().get();
                               ChunkPos playerChunk = client.player.chunkPosition();

                               Map<BlockPos, DeviceSummary> devices = new HashMap<>();

                               // Scansiona la griglia dei chunk visibili attorno al giocatore
                               for (int dx = -renderDistance; dx <= renderDistance; dx++)
                               {
                                   for (int dz = -renderDistance; dz <= renderDistance; dz++)
                                   {
                                       int cx = playerChunk.x() + dx;
                                       int cz = playerChunk.z() + dz;

                                       if (!chunkSource.hasChunk(cx, cz))
                                       {
                                           continue;
                                       }

                                       LevelChunk chunk = chunkSource.getChunk(cx, cz, ChunkStatus.FULL, false);
                                       if (chunk == null)
                                       {
                                           continue;
                                       }

                                       // Il chunk è caricato
                                       for (BlockEntity entity : chunk.getBlockEntities().values())
                                       {
                                           if (entity instanceof NetworkingBlockEntity netEntity)
                                           {
                                               DeviceSummary device = new DeviceSummary(netEntity.getHostname(),
                                                                                        netEntity.getClass().getSimpleName(),
                                                                                        netEntity.getBlockPos());
                                               devices.put(netEntity.getBlockPos(), device);
                                           }
                                       }
                                   }
                               }

                               queryFuture.complete(devices);
                           }
                           catch (Exception e)
                           {
                               queryFuture.completeExceptionally(e);
                           }
                       });

        return queryFuture;
    }


    public static @NonNull CompletableFuture<BlockEntity> getBlockEntityFuture(@NonNull BlockPos pos)
    {
        Minecraft client = Minecraft.getInstance();
        CompletableFuture<BlockEntity> queryFuture = new CompletableFuture<>();

        // Spediamo l'operazione sul main thread del client di gioco
        client.execute(() ->
                       {
                           try
                           {
                               if (client.level == null)
                               {
                                   queryFuture.complete(null);
                                   return;
                               }

                               queryFuture.complete(client.level.getBlockEntity(pos));
                           }
                           catch (Exception e)
                           {
                               queryFuture.completeExceptionally(e);
                           }
                       });

        return queryFuture;
    }


    public static @NonNull CompletableFuture<List<String>> getBlockEntityCliHistoryFuture(@NonNull BlockPos pos)
    {
        Minecraft client = Minecraft.getInstance();
        CompletableFuture<List<String>> queryFuture = new CompletableFuture<>();

        // Spediamo l'operazione sul main thread del client di gioco
        client.execute(() ->
                       {
                           try
                           {
                               if (client.level == null || client.player == null)
                               {
                                   queryFuture.complete(List.of());
                                   return;
                               }

                               // Registriamo la Future prima di trasmettere
                               PENDING_REQUESTS.put(pos, queryFuture);

                               GlobalPos globalPos = GlobalPos.of(client.level.dimension(), pos);
                               ClientPlayNetworking.send(new WebuiCliHistoryC2SPayload(globalPos));
                           }
                           catch (Exception e)
                           {
                               PENDING_REQUESTS.remove(pos);
                               queryFuture.completeExceptionally(e);
                           }
                       });

        return queryFuture;
    }

    public static void completePendingRequest(@NonNull WebuiCliHistoryS2CPayload payload)
    {
        BlockPos pos = payload.pos().pos();

        CompletableFuture<List<String>> pending = ClientWorldScanner.PENDING_REQUESTS.remove(pos);

        if (pending != null)
        {
            // Completa la Future attesa dal thread HTTP del Web Server
            pending.complete(payload.history());
        }
    }
}