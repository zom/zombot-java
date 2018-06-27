package im.zom.ractive.models;

import im.zom.ractive.bots.BasicBot;
import org.jxmpp.jid.BareJid;

/**
 * Created by shifar on 7/3/16.
 */
public class Buddy {

    private final BareJid jid;
    private final BasicBot bot;

    public Buddy(BareJid jid, BasicBot bot) {
        this.jid = jid;
        this.bot = bot;
    }

    public BasicBot getBot() {
        return bot;
    }

    public BareJid getJID() {
        return jid;
    }
}
