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
        try {
            return pandoraBotSession.think(sourceBuddyMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return SORRY;
        }
    }
}
