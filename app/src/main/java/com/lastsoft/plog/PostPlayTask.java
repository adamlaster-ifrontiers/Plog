package com.lastsoft.plog;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.util.BGGLogInHelper;
import com.lastsoft.plog.util.HttpUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class PostPlayTask extends AsyncTask<Play, Void, String> {

    Context theContext;
    String bggUsername;
    public PostPlayTask(Context context, String bggUsername){
        this.bggUsername = bggUsername;
        this.theContext = context;
    }

    @Override
    protected String doInBackground(final Play... playToLog) {

        String bggProcess = "false";

        Log.d("V1", "trying to post a play to bgg");

        BGGLogInHelper helper = new BGGLogInHelper(theContext, null);
        if (helper.checkCookies()) {
            DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
            String output = outputFormatter.format(playToLog[0].playDate); // Output : 01/20/2010


            UrlEncodedFormEntity entity;
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("ajax", "1"));
            nvps.add(new BasicNameValuePair("action", "save"));
            nvps.add(new BasicNameValuePair("version", "2"));
            nvps.add(new BasicNameValuePair("objecttype", "thing"));
            nvps.add(new BasicNameValuePair("objectid", GamesPerPlay.getBaseGame(playToLog[0]).gameBGGID));
            nvps.add(new BasicNameValuePair("playdate", output));
            nvps.add(new BasicNameValuePair("dateinput", output));
            //nvps.add(new BasicNameValuePair("length", String.valueOf(60)));
            nvps.add(new BasicNameValuePair("location", "Plog"));
            nvps.add(new BasicNameValuePair("quantity", String.valueOf(1)));
            nvps.add(new BasicNameValuePair("incomplete", "0"));
            nvps.add(new BasicNameValuePair("nowinstats", "0"));
            nvps.add(new BasicNameValuePair("comments", playToLog[0].playNotes));

            List<PlayersPerPlay> playaz = PlayersPerPlay.getPlayers(playToLog[0]);
            for(PlayersPerPlay player:playaz){
                addPair(nvps, player.getId().intValue(), "playerid", "player_" + player.player.getId());
                addPair(nvps, player.getId().intValue(), "name", player.player.playerName);
                addPair(nvps, player.getId().intValue(), "username", player.player.bggUsername);
                addPair(nvps, player.getId().intValue(), "color", player.color);
                //addPair(nvps, player.getId().intValue(), "position", StartingPosition);
                addPair(nvps, player.getId().intValue(), "score", player.score+"");
                //addPair(nvps, player.getId().intValue(), "rating", String.valueOf(Rating));
                //addPair(nvps, player.getId().intValue(), "new", New ? "1" : "0");
                //addPair(nvps, player.getId().intValue(), "win", Win ? "1" : "0");
            }




            try {
                entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
                HttpPost post = new HttpPost("http://www.boardgamegeek.com/geekplay.php");
                post.setEntity(entity);
                HttpClient mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
                HttpResponse response = mClient.execute(post);
                if (response == null) {

                } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

                } else {
                    String theResponse = HttpUtils.parseResponse(response);

                    if (isValidResponse(theResponse)) {
                        if (theResponse.contains("playid")) {
                            //{"playid":"15110214","numplays":"1","html":"Plays: <a href=\"\/plays\/thing\/95527?userid=515783\">1<\/a>"}
                            //in here, play is logged.  therefore, poll and get the id for the most recent play of the game and save the id
                            String[] values = theResponse.split("\\,");
                            String[] playIDarray = values[0].split("\\:");
                            playToLog[0].bggPlayID = playIDarray[1].replace("\"", "");
                            playToLog[0].save();
                        }else {

                            String getPlayIDurl = HttpUtils.constructPlayUrlSpecific(bggUsername, GamesPerPlay.getBaseGame(playToLog[0]).gameBGGID, output);

                            post = new HttpPost(getPlayIDurl);
                            mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
                            response = mClient.execute(post);
                            String theResponse2 = HttpUtils.parseResponse(response);

                            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(true);
                            XmlPullParser parser = factory.newPullParser();
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(new StringReader(theResponse2));
                            //parser.nextTag();
                            // parser.require(XmlPullParser.START_TAG, null, "items");

                            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                                if (parser.getEventType() != XmlPullParser.START_TAG) {
                                    continue;
                                }
                                String name = parser.getName();
                                // Starts by looking for the entry tag
                                if (name.equals("play")) {
                                    //entries.add(readEntry(parser));
                                    String readPlayID = "";
                                    readPlayID = readPlayID(parser);
                                    playToLog[0].bggPlayID = readPlayID;
                                    playToLog[0].save();
                                    break;
                                } else if (name.equals("plays")) {

                                } else {
                                    skip(parser);
                                }
                            }
                        }


                    } else {
                        //savePending(play);
                    }
                }


                //Log.d("V1", response.getStatusLine().);
            } catch (UnsupportedEncodingException e) {
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }


        }


        return bggProcess;
    }

    private String readPlayID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("play")) {
            bggid = parser.getAttributeValue(null, "id");
        }
        return bggid;
    }


    private void addPair(List<NameValuePair> nvps, int index, String key, String value) {
        nvps.add(new BasicNameValuePair("players[" + index + "][" + key + "]", value));
    }

    public boolean isValidResponse(String mResponse) {
        if (TextUtils.isEmpty(mResponse)) {
            return false;
        }
        return mResponse.startsWith("Plays: <a") || mResponse.startsWith("{\"html\":\"Plays:")|| mResponse.startsWith("{\"playid\":");
    }

    @Override
    protected void onPostExecute(final String result) {
        if (result.equals("true")) {
            Toast.makeText(theContext, theContext.getString(R.string.bgg_process_notice), Toast.LENGTH_LONG).show();
        }
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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


}