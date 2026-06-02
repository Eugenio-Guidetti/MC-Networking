package eu.eugenioguidetti.mcnetworking.component;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Questo record serve a memorizzare lo stato di un item cavo dopo il primo click
 *
 * @author Eugenio Guidetti
 */
public record PendingConnection(BlockPos pos, Direction face)
{
    // Il Codec è necessario per permettere a Minecraft di salvare/caricare questo dato su disco e mandarlo in rete (Multiplayer)
    public static final Codec<PendingConnection> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.fieldOf("pos").forGetter(PendingConnection::pos),
                   Direction.CODEC.fieldOf("face").forGetter(PendingConnection::face))
            .apply(instance, PendingConnection::new));
}