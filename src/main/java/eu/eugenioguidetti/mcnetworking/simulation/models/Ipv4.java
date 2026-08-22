package eu.eugenioguidetti.mcnetworking.simulation.models;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/08/2026
 */

import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class Ipv4
{
    /**
     *
     * @return un int con la rappresentazione binaria dell'indirizzo ip specificato
     */
    public static int parseIp(String ipString)
    {
        if (ipString == null || ipString.isEmpty())
        {
            throw new IllegalArgumentException("ipString vuota");
        }

        int rawIp = 0;

        String[] ottetti = ipString.split("\\.");

        if (ottetti.length != 4)
        {
            throw new IllegalArgumentException("Numero di ottetti errato");
        }

        for (int i = 0; i < 4; i++)
        {
            int ottetto = Integer.parseInt(ottetti[i]);

            if (ottetto < 0 || ottetto > 255)
            {
                throw new IllegalArgumentException("Valore ottetto errato");
            }

            rawIp <<= 8;
            rawIp |= ottetto;
        }

        return rawIp;
    }


    /**
     *
     * @return La rappresentazione in notazione decimale puntata dell'indirizzo rawIp fornito
     */
    public static @NonNull String formatRawIpToString(int rawIp)
    {
        return String.format("%d.%d.%d.%d",
                             (rawIp >> 24) & 0x000000FF,
                             (rawIp >> 16) & 0x000000FF,
                             (rawIp >> 8) & 0x000000FF,
                             rawIp & 0x000000FF);
    }
}
