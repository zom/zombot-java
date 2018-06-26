package im.zom.ractive.models;

import im.zom.ractive.bots.BasicBot;

/**
 * Created by shifar on 7/3/16.
 */
public class Buddy {

    private final String email;
    private final BasicBot bot;

    public Buddy(String email, BasicBot bot) {
        this.email = email;
        this.bot = bot;
    }

    public BasicBot getBot() {
        return bot;
    }

    public String getEmail() {
        return email;
    }
}
