package eu.eugenioguidetti.mcnetworking.terminal;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.networking.packet.TerminalOutputS2CPayload;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */
public class ConsoleSession
{
    private final BlockPos pos;
    private final ServerLevel level;
    private final GlobalPos globalPos;

    private final NetworkingBlockEntity device;
    private TerminalMode currentMode = TerminalMode.USER_EXEC;

    // Se siamo in (config-if), qui salviamo quale interfaccia stiamo modificando
    private String selectedInterfaceName = null;

    public ConsoleSession(BlockPos pos, @NonNull ServerLevel level, NetworkingBlockEntity device)
    {
        this.pos = pos;
        this.level = level;
        this.device = device;

        this.globalPos = GlobalPos.of(level.dimension(), pos);
    }

    public void sendOutput(String output)
    {
        TerminalCache.renderOutput(level, pos, output);

        updateOutput();
    }

    public void updateOutput()
    {
        TerminalCache.CacheValue cached = TerminalCache.getOrCreateSession(level, pos);

        List<String> historySnapshot = List.copyOf(cached.history());

        for (ServerPlayer player : PlayerLookup.around(level, pos, 16))
        {
            ServerPlayNetworking.send(player,
                                      new TerminalOutputS2CPayload(historySnapshot,
                                                                   cached.session().getDevice().getForegroundJob().isEmpty(),
                                                                   this.getPrompt(),
                                                                   globalPos));
        }
    }

    public void sendError(String error)
    {
        sendOutput(String.format(Component.translatable("mcnetworking.cli.error_format").getString(), error));
    }

    public void sendError(String error, @NonNull Exception e)
    {
        sendError(error + ": " + e.getMessage());
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public ServerLevel getLevel()
    {
        return level;
    }

    public NetworkingBlockEntity getDevice()
    {
        return device;
    }

    public TerminalMode getCurrentMode()
    {
        return currentMode;
    }

    public void setCurrentMode(TerminalMode currentMode)
    {
        this.currentMode = currentMode;

        updateOutput();
    }

    public NetworkInterface getSelectedInterface()
    {
        return device.getInterface(this.selectedInterfaceName);
    }

    public String getSelectedInterfaceName()
    {
        return selectedInterfaceName;
    }

    public void selectInterface(String selectedInterfaceName)
    {
        if (selectedInterfaceName != null && selectedInterfaceName.equals(LOOPBACK_NAME))
        {
            throw new IllegalArgumentException("Non puoi selezionare l'interfaccia di loopback");
        }

        this.selectedInterfaceName = selectedInterfaceName;
    }

    public void deselectInterface()
    {
        this.selectedInterfaceName = null;
    }

    public String getPrompt()
    {
        return device.getHostname() + currentMode.getPromptSuffix();
    }

    @Override
    public String toString()
    {
        return "ConsoleSession{" + "device=" + device + ", currentMode=" + currentMode + '}';
    }
}