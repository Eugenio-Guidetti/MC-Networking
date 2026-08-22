package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */
public class RoutingTable
{
    private final List<Route> table = new ArrayList<>();

    private final AbstractL3NetworkingBlockEntity l3NetEntity;

    public RoutingTable(AbstractL3NetworkingBlockEntity l3NetEntity)
    {
        this.l3NetEntity = l3NetEntity;
    }

    public void addRoute(RouteType type, Ipv4CidrAddress destNetwork, Ipv4Address nextHop, int cost, String nicName)
    {
        addRoute(new Route(type, destNetwork, nextHop, cost, nicName));
    }

    public void addRoute(@NonNull Route r)
    {
        removeRoute(r.destNetwork);
        table.add(r);
    }

    public void removeRoute(Ipv4CidrAddress destNetwork)
    {
        table.removeIf(r -> r.destNetwork.equals(destNetwork));
    }

    public List<Route> getRoutes()
    {
        List<Route> routes = new ArrayList<>();

        // Rete di loopback
        routes.add(new Route(RouteType.L, Ipv4CidrAddress.LOOPBACK, null, 0, LOOPBACK_NAME));

        // Limited broadcast
        routes.add(new Route(RouteType.L, Ipv4CidrAddress.BROADCAST, null, 0, LOOPBACK_NAME));

        for (NetworkInterface nic : l3NetEntity.getNics().values())
        {
            String nicName = nic.getName();
            Ipv4CidrAddress nicAddress = nic.getIpAddress();

            // Salta interfacce di loopback, non configurate o non connesse
            if (nic.isLoopback() || !nic.isConnected() || nicAddress.address().equals(Ipv4Address.ALL_ZEROS))
            {
                continue;
            }

            // Indirizzi locali (ip interfaccia, rete e broadcast)
            routes.add(new Route(RouteType.L, new Ipv4CidrAddress(nicAddress.getIndirizzoDiRete().address(), 32), null, 0, nicName));
            routes.add(new Route(RouteType.L, new Ipv4CidrAddress(nicAddress.address(), 32), null, 0, nicName));
            routes.add(new Route(RouteType.L, new Ipv4CidrAddress(nicAddress.getIndirizzoDiBroadcast().address(), 32), null, 0, nicName));

            // Reti connesse
            routes.add(new Route(RouteType.C, nicAddress.getIndirizzoDiRete(), null, 0, nicName));
        }

        routes.addAll(table);

        routes.sort((r1, r2) -> Integer.compare(r2.destNetwork.subnetMask().lunghezzaPrefisso(),
                                                r1.destNetwork.subnetMask().lunghezzaPrefisso()));

        return routes;
    }

    public List<Route> getStaticRoutes()
    {
        List<Route> staticRoutes = new ArrayList<>(table);

        staticRoutes.removeIf(route -> !route.type().equals(RouteType.S));

        return staticRoutes;
    }

    public Route routePacket(Ipv4Address destIp)
    {
        List<Route> routes = getRoutes();

        routes.removeIf(r -> !r.destNetwork().contieneIp(destIp));

        routes.removeIf(r -> !(l3NetEntity.getNics().get(r.nicName).isLoopback() || l3NetEntity.getNics().get(r.nicName).isConnected()));

        if (routes.isEmpty())
        {
            return null;
        }

        routes.sort(Route::compareTo);

        return routes.getFirst();
    }

    public enum RouteType
    {
        L, // Local (Il router gestisce/analizza il pacchetto)
        C, // Connected (Rete direttamente connessa al router)
        S, // Static
        R, // RIP
    }

    public record Route(@NonNull RouteType type, @NonNull Ipv4CidrAddress destNetwork, Ipv4Address nextHop, int costo,
                        @NonNull String nicName)
    {
        public int compareTo(@NonNull Route other)
        {
            return Integer.compare(this.costo, other.costo);
        }


        public void save(@NonNull ValueOutput output)
        {
            output.putString("Type", type.name());
            output.putString("DestNetwork", destNetwork.toString());
            if (nextHop != null)
            {
                output.putString("NextHop", nextHop.toString());
            }
            output.putInt("Costo", costo);
            output.putString("NicName", nicName);
        }

        public static @Nullable Route load(ValueInput input)
        {
            try
            {
                RouteType type = RouteType.valueOf(input.getString("Type").orElseThrow());
                Ipv4CidrAddress destNetwork = new Ipv4CidrAddress(input.getString("DestNetwork").orElseThrow());
                Ipv4Address nextHop = new Ipv4Address(input.getString("NextHop").orElse(null));
                int costo = input.getInt("Costo").orElse(0);
                String nicName = input.getString("NicName").orElseThrow();

                return new Route(type, destNetwork, nextHop, costo, nicName);
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }
}
