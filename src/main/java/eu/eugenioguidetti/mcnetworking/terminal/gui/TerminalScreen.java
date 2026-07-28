package eu.eugenioguidetti.mcnetworking.terminal.gui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.terminal.packet.TerminalCommandC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
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
    private List<FormattedCharSequence> visualLines = null;
    private EditBox inputField;
    private String currentPrompt = null;

    private int historyIndex = 0;
    private String draftCommand = "";
    private int scrollOffset = 0;

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

        int promptWidth = this.font.width(currentPrompt);
        int inputFieldX = 10 + promptWidth;
        int inputFieldY = this.height - 20;
        int inputFieldWidth = this.width - 20 - promptWidth;
        int inputFieldHeight = 12;

        if (this.inputField == null)
        {
            this.inputField = new EditBox(this.font, inputFieldX, inputFieldY, inputFieldWidth, inputFieldHeight, Component.empty());
        }
        else
        {
            this.inputField.setX(inputFieldX);
            this.inputField.setY(inputFieldY);
            this.inputField.setWidth(inputFieldWidth);
        }

        this.inputField.setMaxLength(256);
        this.inputField.setBordered(false);
        this.inputField.setTextColor(GREEN);

        this.addRenderableWidget(this.inputField);
        this.setInitialFocus(this.inputField);
    }

    // Chiudo la UI se il blocco viene distrutto
    @Override
    public void tick()
    {
        super.tick();

        if (this.minecraft == null || this.minecraft.level == null)
        {
            return;
        }

        BlockEntity blockEntity = this.minecraft.level.getBlockEntity(this.pos);

        if (!(blockEntity instanceof NetworkReceiver))
        {
            this.onClose();
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        if (this.getFocused() == null)
        {
            this.setFocused(this.inputField);
        }

        // 1. Sfondo completamente nero (usiamo graphics al posto del vecchio guiGraphics)
        graphics.fill(0, 0, this.width, this.height, BACKGROUND);

        // 2. Disegniamo lo storico (partendo dal basso verso l'alto, appena sopra l'input)
        visualLines = getVisualLines();
        int yOffset = this.height - 35;

        int startIndex = visualLines.size() - 1 - scrollOffset;

        for (int i = startIndex; i >= 0; i--)
        {
            if (yOffset < 0)
            {
                break; // Non disegniamo fuori dallo schermo in alto
            }
            graphics.text(this.font, visualLines.get(i), 10, yOffset, GREEN, false);
            yOffset -= 12; // Spazio tra le righe
        }

        // 3. Disegniamo il PROMPT "finto" esattamente a sinistra della casella di input
        graphics.text(this.font, currentPrompt, 10, this.height - 20, GREEN, false);

        // 4. Scroll bar
        this.renderScrollbar(graphics);

        // 5. Renderizziamo i widget (inclusa la casella di testo) passando i nuovi parametri
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event)
    {
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER)
        {
            String command = this.inputField.getValue().trim();

            if (!command.isEmpty())
            {
                this.scrollOffset = 0;
            }

            ClientPlayNetworking.send(new TerminalCommandC2SPacket(this.pos, command));

            // Aggiungi il comando digitato allo storico locale per vederlo
            history.add(currentPrompt + command);
            visualLines = getVisualLines();

            CommandHistoryCache.addCommand(pos, command);

            if (command.toLowerCase().startsWith("clear"))
            {
                history.clear();
                visualLines.clear();
            }

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

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY)
    {
        // 1. Calcoliamo quante righe di testo entrano al massimo nell'area visibile
        int maxVisibleLines = (this.height - 35) / 12;

        // 2. Il massimo scorrimento possibile evita di andare oltre la riga più vecchia
        int maxScroll = Math.max(0, visualLines.size() - maxVisibleLines);

        // 3. Modifichiamo l'offset in base alla direzione della rotella (scorriamo di 3 righe alla volta)
        if (scrollY > 0)
        {
            // Rotella verso l'alto: andiamo indietro nella cronologia (verso i messaggi vecchi)
            this.scrollOffset = Math.min(maxScroll, this.scrollOffset + 3);
        }
        else if (scrollY < 0)
        {
            // Rotella verso il basso: torniamo verso i messaggi recenti
            this.scrollOffset = Math.max(0, this.scrollOffset - 3);
        }

        return true;
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics)
    {
        int maxVisibleLines = (this.height - 35) / 12;
        int maxScroll = Math.max(0, visualLines.size() - maxVisibleLines);

        // Se tutto il testo entra nello schermo, non disegniamo la scrollbar
        if (maxScroll <= 0)
        {
            return;
        }

        // Dimensioni e coordinate della traccia (il binario della scrollbar)
        int trackX = this.width - 7;
        int trackWidth = 2;
        int trackBottom = this.height - 26;
        int trackHeight = maxVisibleLines * 12;
        int trackTop = trackBottom - trackHeight;

        // 1. Disegniamo lo sfondo della barra (verde molto scuro/trasparente)
        graphics.fill(trackX, trackTop, trackX + trackWidth, trackBottom, 0xFF003300);

        // 2. Calcoliamo l'altezza dell'indicatore (thumb) dinamica, con un minimo di 15 pixel
        int thumbHeight = Math.max(15, (int) ((float) maxVisibleLines / visualLines.size() * trackHeight));
        int availableScrollSpace = trackHeight - thumbHeight;

        // 3. Calcoliamo la posizione Y dell'indicatore.
        // Quando scrollOffset è 0 (in fondo), l'indicatore deve essere in basso (trackTop + availableScrollSpace).
        float scrollFraction = (float) this.scrollOffset / maxScroll;
        int thumbY = trackTop + (int) ((1.0f - scrollFraction) * availableScrollSpace);

        // 4. Disegniamo l'indicatore della scrollbar (usiamo il verde brillante standard del terminale)
        graphics.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, GREEN);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    // Metodo chiamato quando ricevo un pacchetto S2C dal server
    public void addOutput(String output, String newPrompt, @NonNull GlobalPos globalPos)
    {
        GlobalPos terminalPos = GlobalPos.of(this.level.dimension(), this.pos);
        if (!globalPos.equals(terminalPos))
        {
            return;
        }

        if (output != null && !output.isEmpty())
        {
            for (String s : output.split("\n"))
            {
                if (s.isEmpty())
                {
                    continue;
                }

                if (s.replaceFirst(newPrompt, "").toLowerCase().startsWith("clear"))
                {
                    history.clear();
                    visualLines.clear();

                    continue;
                }

                history.add(s);
                visualLines = getVisualLines();
            }
        }

        if (newPrompt != null && !newPrompt.isEmpty() && !newPrompt.equals(currentPrompt))
        {
            currentPrompt = newPrompt;
        }

        this.init();
    }

    private @NonNull List<FormattedCharSequence> getVisualLines()
    {
        List<FormattedCharSequence> visualLines = new ArrayList<>();
        // Calcoliamo la larghezza massima disponibile per il testo (es. larghezza schermo meno 20 pixel di margini)
        int maxLineWidth = Math.max(10, this.width - 20);

        for (String line : history)
        {
            // font.split spezza la stringa in base ai pixel e al word-wrap di Minecraft
            visualLines.addAll(this.font.split(Component.literal(line), maxLineWidth));
        }

        return visualLines;
    }
}