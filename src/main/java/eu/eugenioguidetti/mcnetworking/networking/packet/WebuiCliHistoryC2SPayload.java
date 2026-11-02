package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/11/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public record WebuiCliHistoryC2SPayload(GlobalPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<WebuiCliHistoryC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "webui_history_reply"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WebuiCliHistoryC2SPayload> CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC,
                                                                                                                      WebuiCliHistoryC2SPayload::pos,
                                                                                                                      WebuiCliHistoryC2SPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
