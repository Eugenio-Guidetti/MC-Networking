package eu.eugenioguidetti.mcnetworking.networking;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/08/2026
 */

import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.networking.packet.*;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalSignal;
import eu.eugenioguidetti.mcnetworking.terminal.command.Commands;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Eugenio Guidetti
 */
public class ServerboundPackets
{
    private static final Commands commands = new Commands();

    public static void handleTerminalCommandC2SPacket(@NonNull TerminalCommandC2SPayload payload,
                                                      ServerPlayNetworking.@NonNull Context context)
    {
        String rawCommand = payload.command();
        GlobalPos globalPos = payload.pos();

        ServerLevel level = context.server().getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();

        // Recupera la sessione del giocatore (o creala se è il primo comando) dalla cache sul server
        TerminalCache.CacheValue cached = TerminalCache.getOrCreateSession(level, pos);

        String prompt = cached.session().getPrompt();
        String line = prompt + rawCommand;

        TerminalCache.renderOutput(level, pos, line + "\n");

        List<String> historySnapshot = List.copyOf(cached.history());

        // Invia il comando ricevuto agli altri client
        for (ServerPlayer player : PlayerLookup.around(level, pos, 16))
        {
            ServerPlayNetworking.send(player, new TerminalOutputS2CPayload(historySnapshot, true, cached.session().getPrompt(), globalPos));
        }

        // Fai processare il comando al Registry
        commands.parseInput(cached.session(), rawCommand);
    }

    public static void handleTerminalSignalC2SPacket(@NonNull TerminalSignalC2SPayload payload,
                                                     ServerPlayNetworking.@NonNull Context context)
    {
        TerminalSignal signal = payload.signal();

        GlobalPos globalPos = payload.pos();
        ServerLevel level = context.server().getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();

        ConsoleSession session = TerminalCache.getOrCreateSession(level, pos).session();

        Optional<Job> foundJob = session.getDevice().getLastJob();
        if (foundJob.isPresent())
        {
            foundJob.get().handleSignal(signal);
        }
        else
        {
            session.sendOutput(session.getPrompt());
        }

        String text = switch (signal)
        {
            case SIGINT -> "^C\n";
        };

        session.sendOutput(text);
    }

    public static void handleCommandCompletionC2SPayload(@NonNull CommandCompletionC2SPayload payload,
                                                         ServerPlayNetworking.@NonNull Context context)
    {
        GlobalPos globalPos = payload.pos();
        ServerLevel level = context.server().getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();

        ConsoleSession session = TerminalCache.getOrCreateSession(level, pos).session();

        List<String> foundCommands = commands.autocompleteCommand(session, payload.draftCommand());

        if (foundCommands.size() == 1)
        {
            ServerPlayNetworking.send(context.player(), new CommandCompletionS2CPayload(foundCommands.getFirst() + " "));
        }
        else
        {
            String availableCommands = Utils.listAvailableCommands(foundCommands, payload.draftCommand());
            session.sendOutput(availableCommands);
        }
    }
}
