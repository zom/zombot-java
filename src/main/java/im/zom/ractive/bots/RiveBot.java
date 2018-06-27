package im.zom.ractive.bots;

import com.rivescript.Config;
import com.rivescript.RiveScript;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class RiveBot extends BasicBot {

    RiveScript bot;
    String user;

    public RiveBot (String user, String riveScriptDir)
    {
        this.user = user;

// To enable UTF-8 mode, you'd have initialized the bot like:
        bot = new RiveScript(Config.utf8());

// Load a directory full of RiveScript documents (.rive files)
        bot.loadDirectory(riveScriptDir);

// Sort the replies after loading them!
        bot.sortReplies();

    }

    @Override
    public ArrayList<String> getWhatBotThinks(String sourceBuddyMessage) {

        ArrayList<String> resp = new ArrayList<>();

        String reply = bot.reply(user, sourceBuddyMessage);

        if (reply != null && reply.length() > 0) {
            /**
            StringTokenizer st = new StringTokenizer(reply,"\n\n");
            while (st.hasMoreTokens())
                resp.add(st.nextToken());
             **/
            resp.add(reply);

        }

        return resp;
    }

    @Override
    public String getWhatYouThink(String whatBotThinks) {
        return null;
    }
}
