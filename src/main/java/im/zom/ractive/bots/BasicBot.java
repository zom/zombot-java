package im.zom.ractive.bots;


import java.util.ArrayList;

/**
 * Created by shifar on 7/3/16.
 */
public abstract class BasicBot {
    protected static final String SORRY = "¯\\_(ツ)_/¯";

    public abstract ArrayList<String> getWhatBotThinks(String sourceBuddyMessage);

    public abstract String getWhatYouThink(String whatBotThinks);

}
