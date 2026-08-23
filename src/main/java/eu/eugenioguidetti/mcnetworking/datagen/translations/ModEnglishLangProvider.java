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
    public void generateTranslations(HolderLookup.@NonNull Provider provider, @NonNull TranslationBuilder translationBuilder)
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
        translationBuilder.add("mcnetworking.cli.welcome_message", "Welcome to MCNetworking OS\nType \"help\" to get started\n\n");

        translationBuilder.add("mcnetworking.cli.unknown_command_format", "Unknown command: %s\n");
        translationBuilder.add("mcnetworking.cli.ambiguous_command_format", "Ambiguous command: %s\n");
        translationBuilder.add("mcnetworking.cli.incomplete_command_format", "Incomplete command: %s\n");

        translationBuilder.add("mcnetworking.cli.error_format", "§4Error: %s\n");
        translationBuilder.add("mcnetworking.cli.missing_argument_error_format", "Missing argument: %s");
        translationBuilder.add("mcnetworking.cli.invalid_argument_error_format", "Invalid argument: %s");
        translationBuilder.add("mcnetworking.cli.invalid_state_error_format", "Invalid state: %s");
        translationBuilder.add("mcnetworking.cli.generic_error_format", "Generic error: %s");

        translationBuilder.add("mcnetworking.cli.destination_host_unreachable", "Destination host unreachable\n");
        translationBuilder.add("mcnetworking.cli.default_gateway_unreachable", "Default gateway unreachable\n");

        translationBuilder.add("mcnetworking.cli.no_route_found_format", "No route found for: %s\n");

        translationBuilder.add("mcnetworking.cli.arp_request_timeout_format", "ARP request timed out. Packets to %s have been dropped\n");

        translationBuilder.add("mcnetworking.cli.command.available_commands", "Available commands: ");
        translationBuilder.add("mcnetworking.cli.command.no_available_commands_format", "No available commands with: %s\n");

        translationBuilder.add("mcnetworking.cli.command.interface_not_found_format", "Interface: %s not found");

        translationBuilder.add("mcnetworking.cli.command.invalid_subnet_mask_format", "Invalid subnet mask: %s");
        translationBuilder.add("mcnetworking.cli.command.invalid_interface_ip_address_format",
                               "Can't assign this ip address to an interface: %s");
        translationBuilder.add("mcnetworking.cli.command.cant_assign_default_gateway_to_format", "Can't assign a default gateway to an %s");
        translationBuilder.add("mcnetworking.cli.command.invalid_default_gateway_format", "Invalid default gateway: %s");

        translationBuilder.add("mcnetworking.cli.command.invalid_resends_number_format", "Invalid resends number: %s");
        translationBuilder.add("mcnetworking.cli.command.ping.title_format", "\nPinging %s; %s total resends\n\n");
        translationBuilder.add("mcnetworking.cli.command.ping.reply_format", " Reply from %s: duration: %s ticks TTL: %s\n");
        translationBuilder.add("mcnetworking.cli.command.ping.timeout", " Request timed out\n");
        translationBuilder.add("mcnetworking.cli.command.ping.stats_format",
                               "\nPing stats for %s:\n Avg wait: %s ticks. Packets sent: %s of %s. Received: %s. Lost: %s (%s%%)\n\n");

        translationBuilder.add("mcnetworking.cli.command.traceroute.title_format", "\nTracing route to %s with a maximum of %s hops\n\n");
        translationBuilder.add("mcnetworking.cli.command.traceroute.timeout", "Request timed out\n");
        translationBuilder.add("mcnetworking.cli.command.traceroute.trace_completed", "\nTrace complete\n\n");


        translationBuilder.add("mcnetworking.cli.command.network_already_connected",
                               "Another interface already has an address in this network");

        translationBuilder.add("mcnetworking.cli.command.invalid_dest_network", "Invalid destination network address");
        translationBuilder.add("mcnetworking.cli.command.invalid_next_hop", "Invalid next hop address");
        translationBuilder.add("mcnetworking.cli.command.invalid_next_hop_this_router", "Invalid next hop address (it's this router)");

        translationBuilder.add("mcnetworking.cli.command.show.mac.output_format", " MAC: %s\n");
        translationBuilder.add("mcnetworking.cli.command.show.ip.output_format", " IP: %s\n");
        translationBuilder.add("mcnetworking.cli.command.show.pos.output_format", "Dimension: %s; Position: %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.empty", "ARP cache is empty\n");
        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.output_format", "ARP cache:\n%s");
        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.row_format", " %s -> %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.switching_table.empty", "Switching table is empty\n");
        translationBuilder.add("mcnetworking.cli.command.show.switching_table.output_format", "Switching table:\n%s");
        translationBuilder.add("mcnetworking.cli.command.show.switching_table.row_format", " %s -> %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.routing_table.missing", "Missing routing table\n");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.empty", "Routing table is empty\n");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.output_format", "Routing table:\n%s");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.row_format", " %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.interfaces.output", "Interfaces:\n");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.connected", "(connected)\n");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.not_connected", "(not connected)\n");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.loopback", "(loopback)\n");

        translationBuilder.add("mcnetworking.cli.command.description.help",
                               "help Shows available commands\nhelp <command> Shows how to use the specified command\n");
        translationBuilder.add("mcnetworking.cli.command.description.clear", "clear Clears the terminal\n");
        translationBuilder.add("mcnetworking.cli.command.description.configure_format", "configure Goes to the %s configuration mode\n");
        translationBuilder.add("mcnetworking.cli.command.description.enable_format", "enable Goes to the %s configuration mode\n");
        translationBuilder.add("mcnetworking.cli.command.description.end_format", "end Goes to the %s configuration mode\n");
        translationBuilder.add("mcnetworking.cli.command.description.exit", "exit Goes to the previous configuration mode\n");
        translationBuilder.add("mcnetworking.cli.command.description.hostname", "hostname <hostname> Sets the device's hostname\n");
        translationBuilder.add("mcnetworking.cli.command.description.interface_format",
                               "interface <interface_name | interface_direction> Goes to the %s configuration mode for the specified interface\n");

        translationBuilder.add("mcnetworking.cli.command.description.ip",
                               "IP configuration\nip help Shows the available ip configurations\nip help <configuration> Shows how to use the specified configuration\n");
        translationBuilder.add("mcnetworking.cli.command.description.ip.address",
                               "ip address <address> Assigns an IP address to the interface\n");
        translationBuilder.add("mcnetworking.cli.command.description.ip.default_gateway",
                               "ip default_gateway <address> Sets the IP address of the default gateway\n");
        translationBuilder.add("mcnetworking.cli.command.description.ip.route",
                               "ip route <destination_network> <subnet_mask> <next_hop> adds a static route for the specified network through the specified next hop\n");

        translationBuilder.add("mcnetworking.cli.command.description.ping",
                               "ping <dest_ip> [resends] Sends ICMP echo requests to the specified ip address\n");

        translationBuilder.add("mcnetworking.cli.command.description.show",
                               "Information about the device\nshow help Shows the available options\nshow help <option> Shows which information the specified option contains\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.mac",
                               "show mac Shows the MAC address of the selected interface\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.ip", "show ip Shows the IP address of the selected interface\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.pos",
                               "show pos Shows the device's dimension and the position in the game world\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.arp_cache", "show arp_cache Shows the device's ARP cache\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.switching_table",
                               "show switching_table Shows the device's switching table\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.routing_table",
                               "show routing_table Shows the device's routing table\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.interfaces", "show interfaces Shows the device's interfaces\n");

        translationBuilder.add("mcnetworking.cli.command.description.traceroute",
                               "traceroute <dest_ip> [resends_per_hop] Traces the route to reach the specified ip address\n");
    }
}
