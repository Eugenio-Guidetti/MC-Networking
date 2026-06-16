package eu.eugenioguidetti.mcnetworking.terminal.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public record OpenTerminalS2CPacket(BlockPos pos, List<String> history, String currentPrompt) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<OpenTerminalS2CPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "open_terminal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTerminalS2CPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC,
                                                                                                                  OpenTerminalS2CPacket::pos,
                                                                                                                  ByteBufCodecs.STRING_UTF8.apply(
                                                                                                                          ByteBufCodecs.list()),
                                                                                                                  OpenTerminalS2CPacket::history,
                                                                                                                  ByteBufCodecs.STRING_UTF8,
                                                                                                                  OpenTerminalS2CPacket::currentPrompt,
                                                                                                                  OpenTerminalS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return ID;
    }
}