package eu.eugenioguidetti.mcnetworking.datagen.translations;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModEnglishLangProvider extends FabricLanguageProvider
{
    public ModEnglishLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        // Specifying en_us is optional, as it's the default language code
        super(dataOutput, "en_us", registryLookup);
    }


    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider provider, TranslationBuilder translationBuilder)
    {
        // --- Items ---
        translationBuilder.add(ModItems.COPPER_STRAIGHT_CABLE, "Copper Straight Cable");
        translationBuilder.add(ModItems.COPPER_CROSSOVER_CABLE, "Copper Crossover Cable");
        translationBuilder.add(ModItems.FIBER_OPTIC_CABLE, "Fiber Optic Cable");
        translationBuilder.add(ModItems.SCISSORS, "Scissors");

        translationBuilder.add("mcnetworking.cable.cancelled", "Link cancelled");
        translationBuilder.add("mcnetworking.cable.no_interfaces_on_this_side", "No interface on this side");
        translationBuilder.add("mcnetworking.cable.wrong_connector", "Wrong connector");
        translationBuilder.add("mcnetworking.cable.interface_already_connected", "This interface is already connected");
        translationBuilder.add("mcnetworking.cable.link_started_format", "Link %s started from %s");
        translationBuilder.add("mcnetworking.cable.cancelled_already_connected", "Link cancelled. One interface is already connected");
        translationBuilder.add("mcnetworking.cable.link_ended_format", "Devices linked with %s");

        // --- Item tooltips ---
        translationBuilder.add("tooltip.mcnetworking.cable", "§7§nRight click§r§7 to select the first interface");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection", "§7§nRight click§r§7 to select the second interface");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.cancel", "§7§nShift-Right click§r§7 to cancel connection");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.first_interface", "§7First interface at §o%s, face: %s");

        // --- Blocks ---
        translationBuilder.add(ModBlocks.HOST_BLOCK, "Host");
        translationBuilder.add(ModBlocks.HUB_BLOCK, "Hub");
        translationBuilder.add(ModBlocks.SWITCH_BLOCK, "Switch");
        translationBuilder.add(ModBlocks.ROUTER_BLOCK, "Router");

        // --- Creative mode tabs ---
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".cables", "MCNetworking: Cables");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".end_devices", "MCNetworking: End Devices");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".network_devices", "MCNetworking: Network Devices");


        // --- CLI ---
        translationBuilder.add("mcnetworking.cli.welcome_message", "Welcome to MCNetworking OS\nType \"help\" to get started");

        translationBuilder.add("mcnetworking.cli.error_format", "§4Error: %s");

        translationBuilder.add("mcnetworking.cli.unknown_command_format", "Unknown command: %s");
        translationBuilder.add("mcnetworking.cli.missing_argument_error_format", "Missing argument: %s");
        translationBuilder.add("mcnetworking.cli.invalid_argument_error_format", "Invalid argument: %s");
        translationBuilder.add("mcnetworking.cli.invalid_state_error_format", "Invalid state: %s");
        translationBuilder.add("mcnetworking.cli.generic_error_format", "Generic error: %s");

        translationBuilder.add("mcnetworking.cli.destination_host_unreachable", "Destination host unreachable");
        translationBuilder.add("mcnetworking.cli.default_gateway_unreachable", "Default gateway unreachable");

        translationBuilder.add("mcnetworking.cli.no_route_found_format", "No route found for: %s");

        translationBuilder.add("mcnetworking.cli.arp_request_timeout_format", "ARP request timed out. Packets to %s have been dropped");

        translationBuilder.add("mcnetworking.cli.command.available_commands", "Available commands: ");

        translationBuilder.add("mcnetworking.cli.command.interface_not_found_format", "Interface: %s not found");

        translationBuilder.add("mcnetworking.cli.command.invalid_subnet_mask_format", "Invalid subnet mask: %s");
        translationBuilder.add("mcnetworking.cli.command.invalid_interface_ip_address_format",
                               "Can't assign this ip address to an interface: %s");
        translationBuilder.add("mcnetworking.cli.command.cant_assign_default_gateway_to_format", "Can't assign a default gateway to an %s");
        translationBuilder.add("mcnetworking.cli.command.invalid_default_gateway_format", "Invalid default gateway: %s");

        translationBuilder.add("mcnetworking.cli.command.invalid_resends_number_format", "Invalid resends number: %d");
        translationBuilder.add("mcnetworking.cli.command.pinging_message_format", "Pinging %s. %d left");

        translationBuilder.add("mcnetworking.cli.command.show.mac.output_format", "MAC: %s");
        translationBuilder.add("mcnetworking.cli.command.show.ip.output_format", "IP: %s");
        translationBuilder.add("mcnetworking.cli.command.show.pos.output_format", "Dimension: %s; Position: %s");

        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.empty", "ARP cache is empty");
        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.output_format", "ARP cache: %s");
        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.row_format", "\n %s -> %s");

        translationBuilder.add("mcnetworking.cli.command.show.switching_table.empty", "Switching table is empty");
        translationBuilder.add("mcnetworking.cli.command.show.switching_table.output_format", "Switching table: %s");
        translationBuilder.add("mcnetworking.cli.command.show.switching_table.row_format", "\n %s -> %s");

        translationBuilder.add("mcnetworking.cli.command.show.routing_table.missing", "Missing routing table");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.empty", "Routing table is empty");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.output_format", "Routing table: %s");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.row_format", "\n %s");

        translationBuilder.add("mcnetworking.cli.command.show.interfaces.output", "Interfaces:");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.connected", "(connected)");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.not_connected", "(not connected)");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.loopback", "(loopback)");

        translationBuilder.add("mcnetworking.cli.command.description.help",
                               "help Shows available commands\nhelp [command] Shows how to use the specified command");
        translationBuilder.add("mcnetworking.cli.command.description.clear", "clear Clears the terminal");
        translationBuilder.add("mcnetworking.cli.command.description.configure_format", "configure Goes to the %s configuration mode");
        translationBuilder.add("mcnetworking.cli.command.description.enable_format", "enable Goes to the %s configuration mode");
        translationBuilder.add("mcnetworking.cli.command.description.end_format", "end Goes to the %s configuration mode");
        translationBuilder.add("mcnetworking.cli.command.description.exit", "exit Goes to the previous configuration mode");
        translationBuilder.add("mcnetworking.cli.command.description.hostname", "hostname [hostname] Sets the device's hostname");
        translationBuilder.add("mcnetworking.cli.command.description.interface_format",
                               "interface [interface_name | interface_direction] Goes to the %s configuration mode for the specified interface");

        translationBuilder.add("mcnetworking.cli.command.description.ip",
                               "IP configuration\nip help Shows the available ip configurations\nip help [configuration] Shows how to use the specified configuration");
        translationBuilder.add("mcnetworking.cli.command.description.ip.address",
                               "ip address [address] Assigns an IP address to the interface");
        translationBuilder.add("mcnetworking.cli.command.description.ip.default_gateway",
                               "ip default_gateway [address] Sets the IP address of the default gateway");

        translationBuilder.add("mcnetworking.cli.command.description.ping",
                               "ping [dest_ip] [message] [<resends>] Sends ping messages (not ICMP packets yet) to another device");

        translationBuilder.add("mcnetworking.cli.command.description.show",
                               "Information about the device\nshow help Shows the available options\nshow help [option] Shows which information the specified option contains");
        translationBuilder.add("mcnetworking.cli.command.description.show.mac", "show mac Shows the MAC address of the selected interface");
        translationBuilder.add("mcnetworking.cli.command.description.show.ip", "show ip Shows the IP address of the selected interface");
        translationBuilder.add("mcnetworking.cli.command.description.show.pos",
                               "show pos Shows the device's dimension and the position in the game world");
        translationBuilder.add("mcnetworking.cli.command.description.show.arp_cache", "show arp_cache Shows the device's ARP cache");
        translationBuilder.add("mcnetworking.cli.command.description.show.switching_table",
                               "show switching_table Shows the device's switching table");
        translationBuilder.add("mcnetworking.cli.command.description.show.routing_table",
                               "show routing_table Shows the device's routing table");
        translationBuilder.add("mcnetworking.cli.command.description.show.interfaces", "show interfaces Shows the device's interfaces");
    }
}
