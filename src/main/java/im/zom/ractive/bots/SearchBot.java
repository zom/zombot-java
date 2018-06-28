package im.zom.ractive.bots;


import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class SearchBot extends BasicBot {

    MediaWikiBot wikiBot;
    WikiModel wikiModel = new WikiModel("https://www.mywiki.com/wiki/${image}", "https://www.mywiki.com/wiki/${title}");

    private final static int MAX_LENGTH = 1000;

    public SearchBot (String login, String pass, String lang)
    {
//Creating a new MediaWikiBot with an informative user agent
        HttpActionClient client = HttpActionClient.builder() //
                .withUrl("https://" + lang + ".wikipedia.org/w/") //
                .withUserAgent("ZomBot", "1.0", "info@zom.im") //
                .withRequestsPerUnit(10, TimeUnit.MINUTES) //
                .build();
        wikiBot = new MediaWikiBot(client);
    }

    @Override
    public ArrayList<String> getWhatBotThinks(String searchTerm) {

        ArrayList<String> resp = new ArrayList<>();

        Article article = wikiBot.getArticle(searchTerm);

        try {

            String plainStr = wikiModel.render(new PlainTextConverter(), article.getText());

            if (plainStr.length() > MAX_LENGTH)
                plainStr = plainStr.substring(0,MAX_LENGTH) + "...";

            plainStr = plainStr.replaceAll("(\\w+):([^\\n]+)","").replace("\n","");

            resp.add(plainStr);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return resp;
    }

    @Override
    public String getWhatYouThink(String whatBotThinks) {
        return null;
    }
}
