package eu.eugenioguidetti.mcnetworking.networking.packet;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 23/08/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
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
public record CommandCompletionS2CPayload(String autocompleted) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<CommandCompletionS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            MCNetworking.MOD_ID,
            "command_completion_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CommandCompletionS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                                                                                                                        CommandCompletionS2CPayload::autocompleted,
                                                                                                                        CommandCompletionS2CPayload::new);

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
