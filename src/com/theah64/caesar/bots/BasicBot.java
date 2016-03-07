package com.theah64.caesar.bots;

import com.google.code.chatterbotapi.ChatterBotFactory;
import javafx.animation.FadeTransitionBuilder;

/**
 * Created by shifar on 7/3/16.
 */
public abstract class BasicBot {
    protected static final String SORRY = "Grrr...";
    private static ChatterBotFactory chatFactory = new ChatterBotFactory();

    public abstract String getWhatBotThinks(String sourceBuddyMessage);

    protected static ChatterBotFactory getChatFactory() {
        return chatFactory;
    }
}
