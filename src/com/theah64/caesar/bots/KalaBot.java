package com.theah64.caesar.bots;

import com.google.code.chatterbotapi.ChatterBotThought;
import com.theah64.caesar.utils.DateUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import javax.xml.ws.Response;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.in;

/**
 * Created by shifar on 7/3/16.
 */
public class KalaBot {

    private static HashMap<String, BotResponse> mKeywordToResponse = null;
    private static SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");

    private final static String SKIP_WORDS = "yes a in are do hi no the i you be we is when who what where why can if yo we hello";

    static class BotResponse {

        String keyword;
        Date date;
        String title;
        String response;
    }

    public static void init ()
    {
        mKeywordToResponse = new HashMap<>();

        String[] files = {"data/zombot-responses-en.csv","data/zombot-responses-bo.csv","data/zombot-responses-zh.csv"};

        int idx = 0;

        for (String queryFile : files) {
            try {
                FileReader fr = (new FileReader(new File(queryFile)));
                Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(fr);
                for (CSVRecord record : records) {
                    System.out.println(record.get(0));
                    BotResponse br = new BotResponse();
                    br.keyword = record.get(0);
                    br.title = record.get(1);
                    br.response = record.get(2);

                    try {
                        if (record.get(3) != null && record.get(3).length() > 0)
                            br.date = sdf.parse(record.get(3));
                    }
                    catch (Exception e)
                    {
                        System.out.println("error parsing date: " + e.getMessage());
                    }

                    mKeywordToResponse.put(br.keyword + ' ' + br.title + ' ' + (idx++), br);
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static ArrayList<String> think(String request) {


        ArrayList<String> response = new ArrayList<>();

        for (String botResponseKey : mKeywordToResponse.keySet()) {

                StringTokenizer requestWords = new StringTokenizer(request);
                BotResponse botResponse = mKeywordToResponse.get(botResponseKey);

                while (requestWords.hasMoreTokens()) {

                    String nextWord = requestWords.nextToken();

                    String nextWordSimple = nextWord.replaceAll("[^a-zA-Z ]", "").toLowerCase();
                    String keywordsSimple = botResponse.keyword.replaceAll("[^a-zA-Z ]", "").toLowerCase();
                    boolean checkSimple = nextWordSimple.trim().length() > 0;
                    if (checkSimple && SKIP_WORDS.contains(nextWordSimple))
                        continue;

                    if ((checkSimple && keywordsSimple.contains(nextWordSimple))||botResponse.keyword.contains(nextWord)) {

                        if (botResponse.date != null
                                && (!DateUtils.isWithinDaysFuture(botResponse.date, 2)))
                            break;

                        StringBuffer sb = new StringBuffer();

                        if (!botResponse.response.startsWith(":")) {
                            sb.append(botResponse.title);

                            if (botResponse.date != null) {
                                sb.append(" ");
                                sb.append(sdf.format(botResponse.date));
                            }

                            sb.append(": ");
                        }

                        sb.append(botResponse.response);

                        response.add(sb.toString());
                        break;
                    }
                }

        }

        return response;
    }

    /**
    private String doSearch (String subject) throws IOException
    {

        URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&format=json&exsentences=1&exintro=&explaintext=&exsectionformat=plain&titles=" + subject.replace(" ", "%20"));

        StringBuffer response = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
            String line = null;
            while (null != (line = br.readLine())) {
                response.append(line);
            }
        }

        JSONObject json = new JSONObject(response.toString());
        JSONObject query = json.getJSONObject("query");
        JSONObject pages = query.getJSONObject("pages");
        for(String key: pages.keySet()) {
            JSONObject page = pages.getJSONObject(key);
            String extract = page.getString("extract");
            if (extract != null && extract.length() > 0)
                return extract;
        }

        return null;
    }*/
}
