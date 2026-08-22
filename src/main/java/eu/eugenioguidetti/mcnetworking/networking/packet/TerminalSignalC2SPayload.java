package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 20/08/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalSignal;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public record TerminalSignalC2SPayload(TerminalSignal signal, GlobalPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TerminalSignalC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "terminal_signal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalSignalC2SPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT
                                                                                                                             .map(id -> TerminalSignal.values()[id],
                                                                                                                                  TerminalSignal::ordinal)
                                                                                                                             .cast(),
                                                                                                                     TerminalSignalC2SPayload::signal,
                                                                                                                     GlobalPos.STREAM_CODEC,
                                                                                                                     TerminalSignalC2SPayload::pos,
                                                                                                                     TerminalSignalC2SPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
