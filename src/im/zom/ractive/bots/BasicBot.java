package im.zom.ractive.bots;

import com.google.code.chatterbotapi.ChatterBotFactory;

import java.util.ArrayList;

/**
 * Created by shifar on 7/3/16.
 */
public abstract class BasicBot {
    protected static final String SORRY = "¯\\_(ツ)_/¯";
    private static ChatterBotFactory chatFactory = new ChatterBotFactory();

    public abstract ArrayList<String> getWhatBotThinks(String sourceBuddyMessage);

    public abstract String getWhatYouThink(String whatBotThinks);

    protected static ChatterBotFactory getChatFactory() {
        return chatFactory;
    }
}
