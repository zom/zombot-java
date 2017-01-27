package com.theah64.caesar.bots;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.ArrayList;

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
    public ArrayList<String> getWhatBotThinks(String sourceBuddyMessage) {

        /**
        final ArrayList<String> kalaSays = KalaBot.think(sourceBuddyMessage);
        if (kalaSays != null && kalaSays.size() > 0) {
            return kalaSays;
        }**/

        ArrayList<String> response = new ArrayList<>();

        try {
            final String whatBotThinks = pandoraBotSession.think(sourceBuddyMessage);
            response.add(getWhatYouThink(whatBotThinks));
        } catch (Exception e) {
            e.printStackTrace();
            response.add(SORRY);

        }

        return response;
    }


    @Override
    public String getWhatYouThink(String whatBotThinks) {

        return whatBotThinks;
    }
}
