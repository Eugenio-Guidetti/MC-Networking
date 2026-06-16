package eu.eugenioguidetti.mcnetworking.terminal.gui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.packet.TerminalCommandC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public class TerminalScreen extends Screen
{
    private final Level level;
    private final BlockPos pos;
    private final int GREEN = 0xFF00FF00;
    private final int BACKGROUND = 0x88000000;
    private List<String> history = null;
    private EditBox inputField;
    private String currentPrompt = null;

    private int historyIndex = 0;
    private String draftCommand = "";

    public TerminalScreen(Level level, BlockPos pos, List<String> history, String currentPrompt)
    {
        super(Component.literal("Terminal"));

        this.level = level;
        this.pos = pos;

        this.history = history;
        this.currentPrompt = currentPrompt;
        this.historyIndex = CommandHistoryCache.getHistorySize(pos);
    }

    @Override
    protected void init()
    {
        super.init();

        // Calcoliamo la larghezza del prompt per spostare l'input
        int promptWidth = this.font.width(currentPrompt);

        // Creiamo la casella di testo in basso.
        // Nota: spostiamo la X iniziale di 'promptWidth' verso destra e riduciamo la larghezza totale.
        this.inputField = new EditBox(this.font, 10 + promptWidth, this.height - 20, this.width - 20 - promptWidth, 12, Component.empty());
        this.inputField.setMaxLength(256);
        this.inputField.setBordered(false); // Rimuove il bordo standard di Minecraft per un look da terminale
        this.inputField.setTextColor(GREEN); // Testo verde

        this.addRenderableWidget(this.inputField);
        this.setInitialFocus(this.inputField);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        // 1. Sfondo completamente nero (usiamo graphics al posto del vecchio guiGraphics)
        graphics.fill(0, 0, this.width, this.height, BACKGROUND);

        // 2. Disegniamo lo storico (partendo dal basso verso l'alto, appena sopra l'input)
        int yOffset = this.height - 35;
        for (int i = history.size() - 1; i >= 0; i--)
        {
            if (yOffset < 0)
            {
                break; // Non disegniamo fuori dallo schermo in alto
            }
            graphics.text(this.font, history.get(i), 10, yOffset, GREEN, false);
            yOffset -= 12; // Spazio tra le righe
        }

        // 3. Disegniamo il PROMPT "finto" esattamente a sinistra della casella di input
        graphics.text(this.font, currentPrompt, 10, this.height - 20, GREEN, false);

        // 4. Renderizziamo i widget (inclusa la casella di testo) passando i nuovi parametri
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER)
        {
            String command = this.inputField.getValue().trim();
            ClientPlayNetworking.send(new TerminalCommandC2SPacket(this.pos, command));

            // Aggiungi il comando digitato allo storico locale per vederlo
            history.add(currentPrompt + command);
            CommandHistoryCache.addCommand(pos, command);

            draftCommand = "";
            historyIndex = CommandHistoryCache.getHistorySize(pos);
            this.inputField.setValue("");

            return true;
        }

        // Scorri indietro
        if (event.key() == GLFW.GLFW_KEY_UP)
        {
            if (CommandHistoryCache.getCommand(pos, historyIndex - 1).isEmpty())
            {
                return true;
            }

            // Se siamo in fondo, salviamo la bozza attuale
            if (historyIndex == CommandHistoryCache.getHistorySize(pos))
            {
                draftCommand = this.inputField.getValue();
            }

            if (historyIndex > 0)
            {
                historyIndex--;
                this.inputField.setValue(CommandHistoryCache.getCommand(pos, historyIndex));
                // Sposta il cursore alla fine del testo caricato
                this.inputField.setCursorPosition(this.inputField.getValue().length());
            }
            return true;
        }

        // Scorri avanti
        if (event.key() == GLFW.GLFW_KEY_DOWN)
        {
            if (CommandHistoryCache.getCommand(pos, historyIndex).isEmpty())
            {
                return true;
            }

            historyIndex++;

            if (historyIndex < CommandHistoryCache.getHistorySize(pos))
            {
                this.inputField.setValue(CommandHistoryCache.getCommand(pos, historyIndex));
                this.inputField.setCursorPosition(this.inputField.getValue().length());
            }
            else if (historyIndex == CommandHistoryCache.getHistorySize(pos))
            {
                // Siamo tornati in fondo, ripristiniamo la bozza
                this.inputField.setValue(draftCommand);
                this.inputField.setCursorPosition(this.inputField.getValue().length());
            }
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_TAB)
        {
            return true;
        }

        return super.keyPressed(event);
    }

    // Metodo chiamato quando ricevi un pacchetto S2C dal server
    public void addOutput(String output, String newPrompt)
    {
        if (output != null && !output.isEmpty())
        {
            history.add(output);
        }

        if (newPrompt != null && !newPrompt.isEmpty())
        {
            currentPrompt = newPrompt;
        }

        this.init();
    }
}