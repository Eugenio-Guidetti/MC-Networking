package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public interface NetworkReceiver
{
    void putInterface(@NotNull NetworkInterface networkInterface);

    NetworkInterface getInterface(@NotNull String nicName);

    NetworkInterface getInterface(Direction face);

    NetworkInterface getInterface(MacAddress macAddress);

    String getInterfaceName(Direction face);

    Map<String, NetworkInterface> getNics();

    void disconnectPhysical(Direction face);

    void disconnectAllPhysical();

    NetworkStack getStack();

    void sync();

    String getHostname();

    int getDeviceLayer();
}