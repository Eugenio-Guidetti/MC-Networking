const params = new URLSearchParams(window.location.search);
const x = params.get("x");
const y = params.get("y");
const z = params.get("z");
const query = `x=${x}&y=${y}&z=${z}`;

const input = document.getElementById("cli-input");
const cliPre = document.getElementById("cli-output");
const cliInputRow = document.getElementById("cli-input-row");
const cliPrompt = document.getElementById("cli-prompt");

// --- Storico comandi (Freccia SU / Freccia GIÙ) ---
const cmdHistory = [];
let historyIndex = 0;
let draftCommand = "";


// --- Connessione SSE ---
if (x !== null && y !== null && z !== null) {

    const eventSource = new EventSource(`/device/cli/stream?${query}`);

    // Ricezione storico CLI e prompt
    eventSource.onmessage = function (event) {
        const data = JSON.parse(event.data);

        cliPre.textContent = data.output;
        cliInputRow.style.display = data.showPrompt ? "" : "none";
        cliPrompt.textContent = data.prompt;

        input.focus()

        // Autoscroll verso il fondo
        cliPre.scrollTop = cliPre.scrollHeight;
    };

    // Ricezione autocompletamento TAB
    eventSource.addEventListener("completion", function (event) {
        input.value = event.data;
        // Posiziona il cursore alla fine del comando completato
        input.setSelectionRange(input.value.length, input.value.length);
    });

    eventSource.onerror = function () {
        console.warn("Connessione SSE persa. Riconnessione automatica in corso...");
    };
}


// --- Gestione tasti ---
document.addEventListener("keydown", function (event) {
    if (event.key === "Tab") {
        event.preventDefault(); // Impedisce di cambiare focus tra elementi HTML

        fetch(`/device/cli/complete?${query}`, {
            method: "POST", body: input.value
        });
        return;
    }

    if (event.key === "ArrowUp") {
        event.preventDefault();

        if (cmdHistory.length === 0) {
            return;
        }

        // Se siamo alla fine, salviamo la bozza che l'utente stava digitando
        if (historyIndex === cmdHistory.length) {
            draftCommand = input.value;
        }

        if (historyIndex > 0) {
            historyIndex--;
            input.value = cmdHistory[historyIndex];
            input.setSelectionRange(input.value.length, input.value.length);
        }
        return;
    }

    if (event.key === "ArrowDown") {
        event.preventDefault();

        if (cmdHistory.length === 0) {
            return;
        }

        if (historyIndex < cmdHistory.length - 1) {
            historyIndex++;
            input.value = cmdHistory[historyIndex];
            input.setSelectionRange(input.value.length, input.value.length);
        } else if (historyIndex === cmdHistory.length - 1) {
            historyIndex++;
            input.value = draftCommand; // Ripristina la bozza iniziale
            input.setSelectionRange(input.value.length, input.value.length);
        }
        return;
    }

    if (event.ctrlKey && event.key.toLowerCase() === "c") {
        // Se l'utente ha selezionato del testo non fare nulla
        if (window.getSelection().toString().length > 0) {
            return;
        }

        event.preventDefault();

        if (input.value.length > 0) {
            input.value = "";
            historyIndex = cmdHistory.length;
            draftCommand = "";
        } else {
            fetch(`/device/cli/signal?${query}&signal=SIGINT`, {method: "POST"});
        }
        return;
    }

    if (event.ctrlKey && event.key.toLowerCase() === "l") {
        event.preventDefault();
        fetch(`/device/cli/send?${query}`, {
            method: "POST", body: "clear"
        });
    }
});


// --- Invio comandi (ENTER) ---
function sendCommand(event) {
    event.preventDefault();
    const cmd = input.value.trim();

    if (cmd) {
        // Aggiunge il comando alla cronologia e aggiorna l'indice
        cmdHistory.push(cmd);
        historyIndex = cmdHistory.length;
        draftCommand = "";
    }

    fetch(`/device/cli/send?${query}`, {
        method: "POST", body: cmd
    }).then(() => {
        input.value = "";
        input.focus();
    });
}


// --- Focus automatico input field ---
input.focus();
document.addEventListener("click", () => {
    if (!window.getSelection().toString()) {
        input.focus();
    }
});