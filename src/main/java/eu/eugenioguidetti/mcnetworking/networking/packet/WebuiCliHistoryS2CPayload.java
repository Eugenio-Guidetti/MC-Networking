package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/11/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public record WebuiCliHistoryS2CPayload(List<String> history, boolean showPrompt, String currentPrompt,
                                        GlobalPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<WebuiCliHistoryS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "webui_history_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WebuiCliHistoryS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8.apply(
                                                                                                                              ByteBufCodecs.list()),
                                                                                                                      WebuiCliHistoryS2CPayload::history,
                                                                                                                      ByteBufCodecs.BOOL,
                                                                                                                      WebuiCliHistoryS2CPayload::showPrompt,
                                                                                                                      ByteBufCodecs.STRING_UTF8,
                                                                                                                      WebuiCliHistoryS2CPayload::currentPrompt,
                                                                                                                      GlobalPos.STREAM_CODEC,
                                                                                                                      WebuiCliHistoryS2CPayload::pos,
                                                                                                                      WebuiCliHistoryS2CPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}