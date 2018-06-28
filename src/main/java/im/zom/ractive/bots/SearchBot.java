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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchBot extends BasicBot {

    MediaWikiBot wikiBot;
    WikiModel wikiModel = new WikiModel("https://www.mywiki.com/wiki/${image}", "https://www.mywiki.com/wiki/${title}");

    private String mLang;

    private final static int MAX_LENGTH = 1000;

    private static final Pattern UNWANTED_SYMBOLS =
            Pattern.compile("(?:--|[\\[\\]{}()+/\\\\])");

    public SearchBot (String login, String pass, String lang)
    {
        mLang = lang;

//Creating a new MediaWikiBot with an informative user agent
        HttpActionClient client = HttpActionClient.builder() //
                .withUrl("https://" + lang + ".wikipedia.org/w/") //
                .withUserAgent("ZomBot", "1.0", "info@zom.im") //
                .withRequestsPerUnit(10, TimeUnit.MINUTES) //
                .build();
        wikiBot = new MediaWikiBot(client);
    }

    public void changeLanguage (String lang)
    {

        mLang = lang;

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


        if (Character.UnicodeBlock.of(searchTerm.charAt(0))==Character.UnicodeBlock.TIBETAN)
        {
            if (!mLang.equalsIgnoreCase("bo"))
            {
                changeLanguage("bo");
            }
        }
        else if (Character.UnicodeBlock.of(searchTerm.charAt(0))==Character.UnicodeBlock.CJK_COMPATIBILITY
                ||Character.UnicodeBlock.of(searchTerm.charAt(0))==Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
        {
            if (!mLang.equalsIgnoreCase("zh"))
            {
                changeLanguage("zh");
            }
        }

        ArrayList<String> resp = new ArrayList<>();

        Article article = wikiBot.getArticle(searchTerm);

        String pageText = article.getText();

        if (article.isRedirect() || pageText.startsWith("#REDIRECT"))
        {
            if (pageText.length() > 12) {
                pageText = pageText.substring(12);
                pageText = pageText.substring(0, pageText.indexOf("]"));
                article = wikiBot.getArticle(pageText);
                pageText = article.getText();
            }
        }
        else if (pageText == null || pageText.length() == 0)
        {
            searchTerm = longestWord(searchTerm);
            searchTerm = searchTerm.replace("?", "");

            article = wikiBot.getArticle(searchTerm);
            pageText = article.getText();

            if (article.getSimpleArticle().isRedirect())
            {
                if (pageText.length() > 12) {
                    pageText = pageText.substring(12);
                    pageText = pageText.substring(0, pageText.length() - 2);
                    article = wikiBot.getArticle(pageText);
                    pageText = article.getText();
                }
            }
        }


        try {

            String plainStr = wikiModel.render(new PlainTextConverter(), pageText);

            if (plainStr != null && plainStr.length() > 0) {
                plainStr = plainStr.replaceAll("(\\w+):([^\\n]+)", " ").replace("\n", " ").trim();
                if (plainStr.length() > MAX_LENGTH)
                    plainStr = plainStr.substring(0, MAX_LENGTH) + "...";

                resp.add(plainStr);
            }
            else
            {
                if (mLang.equalsIgnoreCase("bo"))
                    resp.add("དགོངས་དག་ ང་ཚོས་ཚིག་དེའི་སྐོར་ཅི་ཡང་རྙེད་མ་སོང་།");
                else
                    resp.add("Sorry, we could not find the work you were looking for.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return resp;
    }

    @Override
    public String getWhatYouThink(String whatBotThinks) {
        return null;
    }

    @Override
    public String getWelcomeMessage() {

        String welcome = "Tashi Delek, I am Topgyal. Type in the word or subject you want me to search. For example, type \"rainforest\" to search for a wiki article on rainforest.\n\n";

        welcome +=
                "བཀྲ་ཤིས་བདེ་ལེགས། ཁྱེད་རང་གི་འཚོལ་བཤེར་བྱེད་འདོད་པའི་མིང་ཚིག་གམ་ཡང་ན།\n\n"+
                        "ཐ་སྙད་དེ་གཏགས་རོགས། དཔེར་ན། ནགས་ཚལ་ཞེས་གཏགས་ཏེ།\n\n" +
                        "ཝེ་ཁེ་རིག་མཛོད་ནང་དུ་ནགས་ཚལ་སྐོར་གྱི་རྩོམ་ཡིག་འཚོལ།";
        return welcome;
    }

    private String longestWord (String sentence)
    {
        String [] word = sentence.split(" ");
        String maxlethWord = "";

        for(int i = 0; i < word.length; i++){
            for (int j = 1; j < word.length ; j++) {
                if(word[i].length() >= word[j].length()){
                    if (null == maxlethWord) {
                        maxlethWord = word[i];
                    } else if (maxlethWord.length() <= word[i].length()) {
                        maxlethWord = word[i];
                    }
                }
            }
        }

        return maxlethWord;
    }
}
