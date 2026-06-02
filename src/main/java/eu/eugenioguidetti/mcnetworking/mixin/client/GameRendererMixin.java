package eu.eugenioguidetti.mcnetworking.mixin.client;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/05/2026
 */

import eu.eugenioguidetti.mcnetworking.client.rendering.CablesRenderPipeline;
import eu.eugenioguidetti.mcnetworking.client.rendering.CustomRenderPipeline;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *
 * @author Eugenio Guidetti
 */

// Pulizia risorse utilizzate per il rendering custom, alla chiusura del gioco
@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Inject(method = "close", at = @At("RETURN"))
    private void onGameRendererClose(CallbackInfo ci)
    {
        if (CustomRenderPipeline.getInstance() != null)
        {
            CustomRenderPipeline.getInstance().close();
        }
        if (CablesRenderPipeline.getInstance() != null)
        {
            CablesRenderPipeline.getInstance().close();
        }
    }
}