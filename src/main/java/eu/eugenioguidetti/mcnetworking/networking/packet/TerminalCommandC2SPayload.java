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

/**
 *
 * @author Eugenio Guidetti
 */
public record TerminalCommandC2SPayload(String command, GlobalPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TerminalCommandC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "terminal_command"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalCommandC2SPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                                                                                                                      TerminalCommandC2SPayload::command,
                                                                                                                      GlobalPos.STREAM_CODEC,
                                                                                                                      TerminalCommandC2SPayload::pos,
                                                                                                                      TerminalCommandC2SPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}