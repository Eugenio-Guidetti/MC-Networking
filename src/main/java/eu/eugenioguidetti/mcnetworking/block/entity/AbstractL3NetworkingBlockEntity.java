package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/08/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.protocol.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.logic.protocol.IcmpManager;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class AbstractL3NetworkingBlockEntity extends NetworkingBlockEntity
{
    protected final ArpManager arpManager;
    protected final IcmpManager icmpManager;

    public AbstractL3NetworkingBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState)
    {
        super(type, worldPosition, blockState);

        this.arpManager = new ArpManager(this);
        this.icmpManager = new IcmpManager(this);
    }

    public void sendGratuitousArpRequest(String outName)
    {
        arpManager.sendGratuitousArpRequest(outName);
    }

    public Map<Ipv4Address, MacAddress> getArpCache()
    {
        return arpManager.getArpCache();
    }

    public ArpManager getArpManager()
    {
        return arpManager;
    }


    public IcmpManager getIcmpManager()
    {
        return icmpManager;
    }


    @Override
    public int getDeviceLayer()
    {
        return 3;
    }


    @Override
    public void tickServer(Level level)
    {
        super.tickServer(level);

        arpManager.tick(this);
    }
}
