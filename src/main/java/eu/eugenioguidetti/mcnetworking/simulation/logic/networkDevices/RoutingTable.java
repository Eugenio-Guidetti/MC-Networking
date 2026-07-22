package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/07/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class RoutingTable
{
    private final List<Route> table = new ArrayList<>();

    public void addRoute(RouteType type, Ipv4Address destNetwork, Ipv4Address nextHop, int costo, String nicName)
    {
        removeRoute(destNetwork, nextHop);
        table.add(new Route(type, destNetwork, nextHop, costo, nicName));
    }

    public void removeRoute(Ipv4Address destNetwork, Ipv4Address nextHop)
    {
        table.removeIf(r -> r.destNetwork.equals(destNetwork) && r.nextHop.equals(nextHop));
    }

    public List<Route> getRoutes(Map<String, NetworkInterface> routerNics)
    {
        List<Route> routes = new ArrayList<>();

        for (var entry : routerNics.entrySet())
        {
            if (entry.getKey().equals(NetworkInterface.LOOPBACK_NAME))
            {
                continue;
            }

            // Interfaccia non configurata
            if (entry.getValue().getIpAddress().equals(Ipv4Address.ALL_ZEROS))
            {
                continue;
            }

            String nicName = entry.getKey();
            NetworkInterface nic = entry.getValue();

            routes.add(new Route(RouteType.L, new Ipv4Address(nic.getIpAddress().getIp(), 32), nic.getIpAddress(), 0, nicName));
            routes.add(new Route(RouteType.C, nic.getIpAddress().getIndirizzoDiRete(), null, 0, nicName));
        }

        routes.addAll(table);

        return routes;
    }

    public Route routePacket(Ipv4Address destIp, Map<String, NetworkInterface> routerNics)
    {
        List<Route> routes = getRoutes(routerNics);

        routes.removeIf(r -> !r.destNetwork().contieneIp(destIp));

        if (routes.isEmpty())
        {
            return null;
        }

        routes.sort(Route::compareTo);

        return routes.getFirst();
    }

    public enum RouteType
    {
        L, // Local
        C, // Connected
        S, // Static
        R, // RIP
    }

    public record Route(RouteType type, Ipv4Address destNetwork, Ipv4Address nextHop, int costo, String nicName)
    {
        public int compareTo(Route other)
        {
            return Integer.compare(this.costo, other.costo);
        }
    }
}
