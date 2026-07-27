## Cos'è MC-Networking

**MC-Networking** è una mod per Minecraft creata per simulare reti IP, apparati e protocolli di rete reali all'interno
del gioco.  
Questa mod nasce con un duplice obiettivo: fornire uno strumento didattico interattivo per l'apprendimento del
networking e integrare le potenzialità delle reti informatiche direttamente nelle meccaniche del mondo di gioco.

### Come funziona

La mod introduce nel gioco base nuovi blocchi, che rappresentano i veri e propri apparati hardware, e nuovi oggetti
(item), che fungono da cavi di rete per collegare fisicamente le macchine tra loro.

![Apparati di rete e cavi](/screenshots/1.png)

Interagendo con un apparato è possibile configurarlo e diagnosticarlo tramite una CLI custom, emulata direttamente
nell'interfaccia di gioco.

![Terminale CLI](/screenshots/2.png)

### Funzionalità Attuali

Il progetto è ancora in fase di sviluppo. Al momento sono stati implementati:

* **Apparati di rete:**
    * `Hub`
    * `Switch`
    * `Router`
    * `Host`  (Dispositivi finali, attualmente simulati fino al layer ISO/OSI 3)


* **Motore di Rete:**
    * Simulazione dell'inoltro dei pacchetti sincronizzata con il server, con code di uscita sulle interfacce e tempi di
      trasmissione e propagazione.
    * Incapsulamento e decapsulamento dei payload tra i vari layer.


* **Protocolli:**
    * `ARP` per la risoluzione degli indirizzi MAC.


* **Comandi CLI principali:**
    * `hostname`: per impostare il nome del dispositivo.
    * `interface`: per la gestione delle interfacce di rete (es. `eth0`, `lo`).
    * `ip`: per configurare gli indirizzi delle interfacce e i default gateway sugli host.
    * `show`: per ispezionare le configurazioni, la cache ARP e le rotte.
