package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
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
public record OpenTerminalS2CPayload(List<String> history, boolean showPrompt, String currentPrompt,
                                     GlobalPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<OpenTerminalS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "open_terminal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTerminalS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8.apply(
                                                                                                                          ByteBufCodecs.list()),
                                                                                                                   OpenTerminalS2CPayload::history,
                                                                                                                   ByteBufCodecs.BOOL,
                                                                                                                   OpenTerminalS2CPayload::showPrompt,
                                                                                                                   ByteBufCodecs.STRING_UTF8,
                                                                                                                   OpenTerminalS2CPayload::currentPrompt,
                                                                                                                   GlobalPos.STREAM_CODEC,
                                                                                                                   OpenTerminalS2CPayload::pos,
                                                                                                                   OpenTerminalS2CPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}