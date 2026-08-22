## Changelog v1.1.0-26.2

In this version I have reworked the internal networking and jobs engines, making them more robust.  
I have improved the CLI to support more complex outputs, and the SIGINT signal.  
I have added static routes, manageable using the `ip route` CLI command.  
Finally, I have also implemented the `ICMP` protocol, along with the `ping` and `traceroute` commands.

### New Features:

- `SIGINT (CTRL+C)` signal
- `ip route` CLI command, for static routing
- `ICMP` Protocol implementation
- `ping` CLI command
- `traceroute` CLI command
- Gratuitous ARP requests

![Traceroute command](https://raw.githubusercontent.com/Eugenio-Guidetti/MC-Networking/refs/heads/master/screenshots/traceroute.png)

---

## Changelog v1.1.0-26.2

In questa versione ho riprogettato gli engine interni relativi al networking e ai job per renderli più robusti.   
Ho migliorato la CLI per supportare output più complessi e il segnale SIGINT.   
Ho aggiunto le rotte statiche, configurabili con il comando CLI `ip route`.   
Infine, ho implementato il protocollo `ICMP`, insieme ai comandi `ping` e `traceroute`.

### Nuove Feature:

- Segnale `SIGINT (CTRL+C)`
- Comando CLI `ip route`, per le rotte statiche
- Protocollo `ICMP`
- Comando CLI `ping`
- Comando CLI `traceroute`
- Richieste ARP Gratuitous

![Comando traceroute](https://raw.githubusercontent.com/Eugenio-Guidetti/MC-Networking/refs/heads/master/screenshots/traceroute.png)