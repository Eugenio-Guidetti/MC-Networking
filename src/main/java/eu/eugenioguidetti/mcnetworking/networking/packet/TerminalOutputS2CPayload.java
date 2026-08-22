package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
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
public record TerminalOutputS2CPayload(List<String> screenBuffer, boolean showPrompt, String prompt,
                                       GlobalPos globalPos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TerminalOutputS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "terminal_output"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalOutputS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8.apply(
                                                                                                                             ByteBufCodecs.list()),
                                                                                                                     TerminalOutputS2CPayload::screenBuffer,
                                                                                                                     ByteBufCodecs.BOOL,
                                                                                                                     TerminalOutputS2CPayload::showPrompt,
                                                                                                                     ByteBufCodecs.STRING_UTF8,
                                                                                                                     TerminalOutputS2CPayload::prompt,
                                                                                                                     GlobalPos.STREAM_CODEC,
                                                                                                                     TerminalOutputS2CPayload::globalPos,
                                                                                                                     TerminalOutputS2CPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}