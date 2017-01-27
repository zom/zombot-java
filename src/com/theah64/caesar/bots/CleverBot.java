package com.theah64.caesar.bots;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.ArrayList;

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
            System.out.println("Failed to build clever bot");
        }
    }

    @Override
    public ArrayList<String> getWhatBotThinks(String sourceBuddyMessage) {

        ArrayList<String> response = new ArrayList<String>();

        /**
        final String caesarSays = Caeser.think(sourceBuddyMessage);
        if (caesarSays != null) {
            response.add(caesarSays);
            return response;
        }**/

        try {
            final String wotBotThinks = cleverBotSession.think(sourceBuddyMessage);
            response.add(getWhatYouThink(wotBotThinks));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.add(SORRY);
            return response;
        }
    }

    @Override
    public String getWhatYouThink(String whatBotThinks) {
        return whatBotThinks;
    }


}
