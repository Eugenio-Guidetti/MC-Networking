package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices.EndDeviceL2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingTable;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.ConnectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public class RouterBlockEntity extends AbstractL3NetworkingBlockEntity
{
    // A livello 2 i router si comportano come gli host
    private final EndDeviceL2Engine l2Engine = new EndDeviceL2Engine(this);
    private final RoutingL3Engine l3Engine = new RoutingL3Engine(this);

    public RouterBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlockEntities.ROUTER_BLOCK_ENTITY, pos, blockState);

        this.stack.setL2Engine(l2Engine);
        this.stack.setL3Engine(l3Engine);

        hostname = "Router";

        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth0", pos, Direction.NORTH, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth1", pos, Direction.SOUTH, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth2", pos, Direction.EAST, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth3", pos, Direction.WEST, ConnectorType.RJ45));
    }

    public RoutingTable getRoutingTable()
    {
        return this.l3Engine.getRoutingTable();
    }


    // --- Salvataggio/caricamento rotte statiche in NBT ---

    @Override
    protected void saveAdditional(@NonNull ValueOutput output)
    {
        super.saveAdditional(output);

        List<RoutingTable.Route> staticRoutes = l3Engine.getRoutingTable().getStaticRoutes();

        if (staticRoutes.isEmpty())
        {
            return;
        }

        ValueOutput routingTableOutput = output.child("RoutingTable");

        routingTableOutput.putInt("Size", staticRoutes.size());

        for (int i = 0; i < staticRoutes.size(); i++)
        {
            RoutingTable.Route route = staticRoutes.get(i);
            ValueOutput routeOutput = routingTableOutput.child("Route_" + i);

            route.save(routeOutput);
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input)
    {
        super.loadAdditional(input);

        ValueInput routingTableInput = input.child("RoutingTable").orElse(null);

        if (routingTableInput == null)
        {
            return;
        }

        int size = routingTableInput.getInt("Size").orElse(0);
        List<RoutingTable.Route> staticRoutes = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
        {
            ValueInput routeInput = routingTableInput.child("Route_" + i).orElse(null);

            if (routeInput == null)
            {
                continue;
            }

            RoutingTable.Route r = RoutingTable.Route.load(routeInput);

            if (r == null)
            {
                continue;
            }

            l3Engine.getRoutingTable().addRoute(r);
        }
    }
}
