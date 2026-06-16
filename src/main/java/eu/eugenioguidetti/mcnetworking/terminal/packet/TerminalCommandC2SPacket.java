package eu.eugenioguidetti.mcnetworking.terminal.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 *
 * @author Eugenio Guidetti
 */
public record TerminalCommandC2SPacket(BlockPos pos, String command) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TerminalCommandC2SPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "terminal_command"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalCommandC2SPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC,
                                                                                                                     TerminalCommandC2SPacket::pos,
                                                                                                                     ByteBufCodecs.STRING_UTF8,
                                                                                                                     TerminalCommandC2SPacket::command,
                                                                                                                     TerminalCommandC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return ID;
    }
}