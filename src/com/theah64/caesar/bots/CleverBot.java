package com.theah64.caesar.bots;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

/**
 * Created by shifar on 7/3/16.
 */
public class CleverBot extends BasicBot {

    private ChatterBotSession cleverBotSession;

    public CleverBot() {
        try {
            cleverBotSession = getChatFactory()
                    .create(ChatterBotType.CLEVERBOT)
                    .createSession();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to build celever bot");
        }
    }

    @Override
    public String getWhatBotThinks(String sourceBuddyMessage) {

        final String caesarSays = Caeser.think(sourceBuddyMessage);
        if (caesarSays != null) {
            return caesarSays;
        }

        try {
            final String wotBotThinks = cleverBotSession.think(sourceBuddyMessage);
            return getWhatYouThink(wotBotThinks);
        } catch (Exception e) {
            e.printStackTrace();
            return SORRY;
        }
    }

    @Override
    public String getWhatYouThink(String whatBotThinks) {
        return whatBotThinks;
    }


}
