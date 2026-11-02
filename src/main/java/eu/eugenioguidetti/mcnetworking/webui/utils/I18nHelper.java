package eu.eugenioguidetti.mcnetworking.webui.utils;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/11/2026
 */

import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 *
 * @author Eugenio Guidetti
 */
public class I18nHelper
{
    private static final String BUNDLE_BASE_NAME = "webui.lang.messages";

    public static Locale resolveLocale()
    {
        try
        {
            Minecraft client = Minecraft.getInstance();
            String mcLang = client.options.languageCode;
            return Locale.forLanguageTag(mcLang.replace('_', '-'));
        }
        catch (Throwable ignored)
        {
        }

        return Locale.ENGLISH;
    }

    public static @NonNull Map<String, String> getTranslationMap(Locale locale)
    {
        ResourceBundle bundle;
        try
        {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME,
                                              locale,
                                              ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        }
        catch (MissingResourceException e)
        {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME,
                                              Locale.ENGLISH,
                                              ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        }

        Map<String, String> translations = new HashMap<>();
        Enumeration<String> keys = bundle.getKeys();

        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            // Converte le chiavi del tipo "form.coord_x" in "MSG_FORM_COORD_X" per i placeholder HTML
            String placeholderKey = "MSG_" + key.replace('.', '_').toUpperCase(Locale.ROOT);
            translations.put(placeholderKey, bundle.getString(key));
        }

        // Aggiunge la lingua corrente come variabile per il tag <html lang="...">
        translations.put("CURRENT_LANG", locale.getLanguage());

        return translations;
    }
}