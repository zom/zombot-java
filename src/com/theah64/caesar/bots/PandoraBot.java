package com.theah64.caesar.bots;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

/**
 * Created by shifar on 7/3/16.
 */
public class PandoraBot extends BasicBot {

    private ChatterBotSession pandoraBotSession;

    public PandoraBot() {
        try {
            pandoraBotSession = getChatFactory()
                    .create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477")
                    .createSession();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to build pandora bot");
        }
    }

    @Override
    public String getWhatBotThinks(String sourceBuddyMessage) {

        final String caesarSays = Caeser.think(sourceBuddyMessage);
        if (caesarSays != null) {
            return caesarSays;
        }

        try {
            final String whatBotThinks = pandoraBotSession.think(sourceBuddyMessage);
            return getWhatYouThink(whatBotThinks);
        } catch (Exception e) {
            e.printStackTrace();
            return SORRY;
        }
    }


    @Override
    public String getWhatYouThink(String whatBotThinks) {

        whatBotThinks = whatBotThinks.toLowerCase();

        if (whatBotThinks.contains("peter") || whatBotThinks.contains("pandorabots")) {
            return Caeser.MESSAGE_I_GOT_DEVELOPED_BY_THEAPACHE64;
        }

        return whatBotThinks;
    }
}
