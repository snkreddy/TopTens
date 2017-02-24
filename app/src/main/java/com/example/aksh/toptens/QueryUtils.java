package com.example.aksh.toptens;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.entries;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by aksh on 24/2/17.
 */

public class QueryUtils {
    static String ns = null;
    public static List<Track> fetchData(String urlString) throws XmlPullParserException, IOException {

        InputStream stream = null;
        List<Track> entries=new ArrayList<Track>();
        // Instantiate the parser
//        StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();


//        StringBuilder htmlString = new StringBuilder();
//        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
//        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
//                formatter.format(rightNow.getTime()) + "</em>");

        try {
            stream = downloadUrl(urlString);
            Log.v("parse","parsing stream");
            entries = parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
//
//        // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
//        // Each Entry object represents a single post in the XML feed.
//        // This section processes the entries list to combine each entry with HTML markup.
//        // Each entry is displayed in the UI as a link that optionally includes
//        // a text summary.
//        for (Entry entry : entries) {
//            htmlString.append("<p><a href='");
//            htmlString.append(entry.link);
//            htmlString.append("'>" + entry.title + "</a></p>");
//            // If the user set the preference to include summary text,
//            // adds it to the display.
//            if (pref) {
//                htmlString.append(entry.summary);
//            }
        if(entries.isEmpty());
        Log.v("message","empty entries");
        return entries;
        }



    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    private static InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
    public static List<Track> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            Log.v("in parse method","in parse");
            return readFeed(parser);
        } finally {
            in.close();
        }
    }
    private static List<Track> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Track> entries = new ArrayList<>();


        parser.require(XmlPullParser.START_TAG, ns, "rss");
        Log.v("method read feed","in read feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            Log.v("method read feed","in read feed while");
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                Log.v("method read feed","in read feed while continuing");
                continue;
            }
            String name = parser.getName();
          if(name.equals("channel")) {
              parser.nextTag();
          }
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                Log.v("method read feed","in read feed while got item");
                entries=(readEntry(parser));
            } else {
                Log.v("method read feed","in read feed while skipping");
                skip(parser);
            }
        }
        if(entries.isEmpty());
        Log.v("message","empty entries 2");
        return entries;
    }
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static List<Track> readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Track> var=new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "item");
        int title_count=0,artist_count=0;
        String title = null;
        String summary = null;
        String rank_this_week = null;
        String rank_last_week = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title_count++;
                title = readTitle(parser);
            } else if (name.equals("artist")) {
                artist_count++;
                summary = readArtist(parser);
            } else if (name.equals("rank_this_week")) {
                rank_last_week = readrank_this_week(parser);
            }else if(name.equals("rank_list_week"))
                rank_this_week=readrank_last_week(parser);
            else {
                skip(parser);
            }
            if(title_count==1&&artist_count==1)
            {
                Track track=new Track(title,summary,rank_last_week,rank_this_week);
                var.add(track);
                title_count=artist_count=0;
            }
        }
        Log.v("adapter","adapter"+title+" "+summary+" "+rank_this_week);

       if(var.isEmpty());
       Log.v("message","empty var");
        return var;
    }

    // Processes title tags in the feed.
    private static String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        Log.v("title:",title);
        return title;
    }

//    // Processes link tags in the feed.
//    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
//        String link = "";
//        parser.require(XmlPullParser.START_TAG, ns, "link");
//        String tag = parser.getName();
//        String relType = parser.getAttributeValue(null, "rel");
//        if (tag.equals("link")) {
//            if (relType.equals("alternate")){
//                link = parser.getAttributeValue(null, "href");
//                parser.nextTag();
//            }
//        }
//        parser.require(XmlPullParser.END_TAG, ns, "link");
//        return link;
//    }

    // Processes summary tags in the feed.
    private static String readArtist(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "artist");
        String artist = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "artist");
        Log.v("masg",artist);
        return artist;
    }
    private static String readrank_this_week(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "rank_this_week");
        String rank_this_week = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "rank_this_week");
        Log.v("rank this week:",rank_this_week);
        return rank_this_week;
    }
    private static String readrank_last_week(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "rank_last_week");
        String rank_last_week = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "rank_last_week");
        Log.v("rank last week:",rank_last_week);
        return rank_last_week;
    }
    // For the tags title and summary, extracts their text values.
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        Log.v("rank this week:","in read TEXT");
        return result;
    }

}
