package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 23/08/2026
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
public record CommandCompletionC2SPayload(String draftCommand, GlobalPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<CommandCompletionC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "command_completion_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CommandCompletionC2SPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                                                                                                                        CommandCompletionC2SPayload::draftCommand,
                                                                                                                        GlobalPos.STREAM_CODEC,
                                                                                                                        CommandCompletionC2SPayload::pos,
                                                                                                                        CommandCompletionC2SPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
