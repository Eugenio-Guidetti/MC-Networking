package eu.eugenioguidetti.mcnetworking.datagen.translations;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
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
public class ModItalianLangProvider extends FabricLanguageProvider
{
    public ModItalianLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        super(dataOutput, "it_it", registryLookup);
    }


    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider provider, @NonNull TranslationBuilder translationBuilder)
    {
        // --- Items ---
        translationBuilder.add(ModItems.COPPER_STRAIGHT_CABLE, "Cavo di Rame Straight");
        translationBuilder.add(ModItems.COPPER_CROSSOVER_CABLE, "Cavo di Rame Crossover");
        translationBuilder.add(ModItems.FIBER_OPTIC_CABLE, "Cavo in Fibra Ottica");
        translationBuilder.add(ModItems.SCISSORS, "Forbici");

        translationBuilder.add("mcnetworking.cable.cancelled", "Collegamento annullato");
        translationBuilder.add("mcnetworking.cable.no_interfaces_on_this_side", "Nessuna interfaccia su questo lato");
        translationBuilder.add("mcnetworking.cable.wrong_connector", "Questo cavo non ci entra qui");
        translationBuilder.add("mcnetworking.cable.interface_already_connected", "Questa interfaccia è già collegata");
        translationBuilder.add("mcnetworking.cable.link_started_format", "Collegamento %s iniziato in %s");
        translationBuilder.add("mcnetworking.cable.cancelled_already_connected", "Collegamento annullato. Un'interfaccia è già collegata");
        translationBuilder.add("mcnetworking.cable.link_ended_format", "Dispositivi collegati con %s");

        // --- Item tooltips ---
        translationBuilder.add("tooltip.mcnetworking.cable", "§7§nClick destro§r§7 per selezionare la prima interfaccia");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection",
                               "§7§nClick destro§r§7 per selezionare la seconda interfaccia");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.cancel",
                               "§7§nShift-Click destro§r§7 per annullare la connessione");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.first_interface", "§7Prima interfaccia in §o%s, faccia: %s");

        // --- Blocks ---
        //translationBuilder.add(ModBlocks., "Nome blocco");

        // --- Creative mode tabs ---
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".cables", "MCNetworking: Cavi");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".end_devices", "MCNetworking: Dispositivi Finali");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".network_devices", "MCNetworking: Dispositivi di Rete");


        // --- CLI ---
        translationBuilder.add("mcnetworking.cli.welcome_message",
                               "Benvenuto nel sistema operativo MCNetworking\nDigita \"help\" per iniziare\n\n");

        translationBuilder.add("mcnetworking.cli.unknown_command_format", "Comando sconosciuto: %s\n");

        translationBuilder.add("mcnetworking.cli.error_format", "§4Errore: %s\n");
        translationBuilder.add("mcnetworking.cli.missing_argument_error_format", "Argomento mancante: %s");
        translationBuilder.add("mcnetworking.cli.invalid_argument_error_format", "Argomento invalido: %s");
        translationBuilder.add("mcnetworking.cli.invalid_state_error_format", "Stato invalido: %s");
        translationBuilder.add("mcnetworking.cli.generic_error_format", "Errore generico: %s");

        translationBuilder.add("mcnetworking.cli.destination_host_unreachable", "Host non raggiungibile\n");
        translationBuilder.add("mcnetworking.cli.default_gateway_unreachable", "Default gateway non raggiungibile\n");

        translationBuilder.add("mcnetworking.cli.no_route_found_format", "Nessuna rotta trovata per: %s\n");

        translationBuilder.add("mcnetworking.cli.arp_request_timeout_format", "ARP request scaduta: Scartati pacchetti diretti a: %s\n");

        translationBuilder.add("mcnetworking.cli.command.available_commands", "Comandi disponibili: ");

        translationBuilder.add("mcnetworking.cli.command.interface_not_found_format", "Interfaccia: %s non trovata");

        translationBuilder.add("mcnetworking.cli.command.invalid_subnet_mask_format", "Subnet mask non valida: %s");
        translationBuilder.add("mcnetworking.cli.command.invalid_interface_ip_address_format",
                               "Non puoi assegnare questo indirizzo ad un'interfaccia: %s");
        translationBuilder.add("mcnetworking.cli.command.cant_assign_default_gateway_to_format",
                               "Non puoi assegnare un default gateway ad un %s\n");
        translationBuilder.add("mcnetworking.cli.command.invalid_default_gateway_format", "Default gateway non valido: %s");

        translationBuilder.add("mcnetworking.cli.command.invalid_resends_number_format", "Numero di resend non valido: %s");
        translationBuilder.add("mcnetworking.cli.command.ping.title_format", "\nEsecuzione di Ping %s; %s invii totali\n\n");
        translationBuilder.add("mcnetworking.cli.command.ping.reply_format", " Risposta da %s: durata: %s ticks TTL: %s\n");
        translationBuilder.add("mcnetworking.cli.command.ping.timeout", " Richiesta scaduta\n");
        translationBuilder.add("mcnetworking.cli.command.ping.stats_format",
                               "\nStatistiche Ping per %s:\n Attesa media: %s ticks. Pacchetti inviati: %s su %s. Ricevuti: %s. Persi: %s (%s%%)\n\n");

        translationBuilder.add("mcnetworking.cli.command.traceroute.title_format",
                               "\nCalcolo rotta verso %s con un massimo di %s salti\n\n");
        translationBuilder.add("mcnetworking.cli.command.traceroute.timeout", "Richiesta scaduta\n");
        translationBuilder.add("mcnetworking.cli.command.traceroute.trace_completed", "\nRotta completata\n\n");

        translationBuilder.add("mcnetworking.cli.command.network_already_connected",
                               "Un'altra interfaccia ha già un indirizzo in questa rete");

        translationBuilder.add("mcnetworking.cli.command.invalid_dest_network", "Indirizzo della rete di destinazione invalido");
        translationBuilder.add("mcnetworking.cli.command.invalid_next_hop", "Indirizzo del next hop invalido");
        translationBuilder.add("mcnetworking.cli.command.invalid_next_hop_this_router",
                               "Indirizzo del next hop invalido (appartiene a questo router)");

        translationBuilder.add("mcnetworking.cli.command.show.mac.output_format", " MAC: %s\n");
        translationBuilder.add("mcnetworking.cli.command.show.ip.output_format", " IP: %s\n");
        translationBuilder.add("mcnetworking.cli.command.show.pos.output_format", "Dimensione: %s; Posizione: %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.empty", "L'ARP cache è vuota\n");
        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.output_format", "ARP cache:\n%s");
        translationBuilder.add("mcnetworking.cli.command.show.arp_cache.row_format", " %s -> %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.switching_table.empty", "La switching table è vuota\n");
        translationBuilder.add("mcnetworking.cli.command.show.switching_table.output_format", "Switching table:\n%s");
        translationBuilder.add("mcnetworking.cli.command.show.switching_table.row_format", " %s -> %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.routing_table.missing", "Routing table mancante\n");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.empty", "La routing table è vuota\n");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.output_format", "Routing table:\n%s");
        translationBuilder.add("mcnetworking.cli.command.show.routing_table.row_format", " %s\n");

        translationBuilder.add("mcnetworking.cli.command.show.interfaces.output", "Interfacce:\n");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.connected", "(connessa)\n");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.not_connected", "(non connessa)\n");
        translationBuilder.add("mcnetworking.cli.command.show.interfaces.loopback", "(loopback)\n");

        translationBuilder.add("mcnetworking.cli.command.description.help",
                               "help Mostra i comandi disponibili\nhelp <comando> Mostra come usare il comando specificato\n");
        translationBuilder.add("mcnetworking.cli.command.description.clear", "clear Pulisce il terminale\n");
        translationBuilder.add("mcnetworking.cli.command.description.configure_format",
                               "configure Va alla modalità di configurazione %s\n");
        translationBuilder.add("mcnetworking.cli.command.description.enable_format", "enable Va alla modalità di configurazione %s\n");
        translationBuilder.add("mcnetworking.cli.command.description.end_format", "end Va alla modalità di configurazione %s\n");
        translationBuilder.add("mcnetworking.cli.command.description.exit", "exit Va alla modalità di configurazione precedente\n");
        translationBuilder.add("mcnetworking.cli.command.description.hostname", "hostname <hostname> Imposta l'hostname del dispositivo\n");
        translationBuilder.add("mcnetworking.cli.command.description.interface_format",
                               "interface <nome_interfaccia | direzione_interfaccia> Va alla modalità di configurazione %s per l'interfaccia specificata\n");

        translationBuilder.add("mcnetworking.cli.command.description.ip",
                               "Configurazione IP\nip help Mostra le configurazioni ip disponibili\nip help <configurazione> Mostra come usare la configurazione ip specificata\n");
        translationBuilder.add("mcnetworking.cli.command.description.ip.address",
                               "ip address <indirizzo> Assegna un indirizzo IP all'interfaccia\n");
        translationBuilder.add("mcnetworking.cli.command.description.ip.default_gateway",
                               "ip default_gateway <indirizzo> Imposta l'indirizzo IP del default gateway\n");
        translationBuilder.add("mcnetworking.cli.command.description.ip.route",
                               "ip route <rete_destinazione> <subnet_mask> <next_hop> aggiunge una rotta statica vero la rete specificata tramite il next hop specificato\n");

        translationBuilder.add("mcnetworking.cli.command.description.ping",
                               "ping <ip_destinazione> [invii] Invia echo request ICMP all'indirizzo ip specificato\n");

        translationBuilder.add("mcnetworking.cli.command.description.show",
                               "Informazioni sull'apparato\nshow help Mostra le opzioni disponibili\nshow help <opzione> Mostra quali informazioni contiene l'opzione specificata\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.mac",
                               "show mac Mostra l'indirizzo MAC dell'interfaccia selezionata\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.ip",
                               "show ip Mostra l'indirizzo IP dell'interfaccia selezionata\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.pos",
                               "show pos Mostra la dimensione e la posizione nel mondo di gioco dell'apparato\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.arp_cache", "show arp_cache Mostra la cache ARP dell'apparato\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.switching_table",
                               "show switching_table Mostra la tabella di switching dell'apparato\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.routing_table",
                               "show routing_table Mostra la tabella di routing dell'apparato\n");
        translationBuilder.add("mcnetworking.cli.command.description.show.interfaces",
                               "show interfaces Mostra le interfacce dell'apparato\n");

        translationBuilder.add("mcnetworking.cli.command.description.traceroute",
                               "traceroute <ip_destinazione> [invii_per_hop] Calcola la rotta per raggiungere l'indirizzo ip specificato\n");
    }
}
