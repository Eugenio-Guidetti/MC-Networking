package eu.eugenioguidetti.mcnetworking.terminal.initializers;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.command.Commands;
import eu.eugenioguidetti.mcnetworking.terminal.packet.OpenTerminalS2CPacket;
import eu.eugenioguidetti.mcnetworking.terminal.packet.TerminalCommandC2SPacket;
import eu.eugenioguidetti.mcnetworking.terminal.packet.TerminalOutputS2CPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 *
 * @author Eugenio Guidetti
 */
public class ServerTerminalPacketsInitializer implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        Commands commands = new Commands();

        // 1. Registriamo l'esistenza dei pacchetti
        PayloadTypeRegistry.serverboundPlay().register(TerminalCommandC2SPacket.ID, TerminalCommandC2SPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TerminalOutputS2CPacket.ID, TerminalOutputS2CPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenTerminalS2CPacket.ID, OpenTerminalS2CPacket.CODEC);


        // 2. Registriamo cosa fa il SERVER quando riceve il pacchetto dal client
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandC2SPacket.ID, (payload, context) ->
        {
            context.server().execute(() ->
                                     {
                                         BlockPos pos = payload.pos();
                                         ServerLevel level = context.player().level();

                                         String rawCommand = payload.command();

                                         // 1. Recupera la sessione del giocatore (o creala se è il primo comando) dalla cache sul server
                                         TerminalCache.CacheValue cached = TerminalCache.getOrCreateSession(level, pos);
                                         TerminalCache.addLine(level, pos, cached.session().getPrompt() + rawCommand);

                                         // 2. Fai processare il comando al Registry
                                         commands.parseInput(cached.session(), rawCommand);
                                     });
        });
    }
}
