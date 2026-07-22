package eu.eugenioguidetti.mcnetworking.terminal.packet;

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

/**
 *
 * @author Eugenio Guidetti
 */
public record TerminalOutputS2CPacket(String output, String prompt, GlobalPos globalPos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TerminalOutputS2CPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "terminal_output"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalOutputS2CPacket> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                                                                                                                    TerminalOutputS2CPacket::output,
                                                                                                                    ByteBufCodecs.STRING_UTF8,
                                                                                                                    TerminalOutputS2CPacket::prompt,
                                                                                                                    GlobalPos.STREAM_CODEC,
                                                                                                                    TerminalOutputS2CPacket::globalPos,
                                                                                                                    TerminalOutputS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return ID;
    }
}