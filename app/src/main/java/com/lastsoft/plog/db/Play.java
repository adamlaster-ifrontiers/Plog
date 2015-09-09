package com.lastsoft.plog.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;


public class Play extends SugarRecord<Play> {

    public Date playDate;
    public String playNotes;
    public String playPhoto;
    public String bggPlayID;
    public String winners;

    public Play() {
    }

    public Play(Date playDate, String playNotes, String playPhoto, String bggPlayID) {
        this.playDate = playDate;
        this.playNotes = playNotes;
        this.playPhoto = playPhoto;
        this.bggPlayID = bggPlayID;
    }

    public Play(Date playDate, String playNotes, String playPhoto) {
        this.playDate = playDate;
        this.playNotes = playNotes;
        this.playPhoto = playPhoto;
    }

    public Play(Date playDate, String playNotes) {
        this.playDate = playDate;
        this.playNotes = playNotes;
        this.playPhoto = "";
    }

    public Play(Date playDate) {
        this.playDate = playDate;
        this.playNotes = "";
        this.playPhoto = "";
    }


    public static Play findPlayByBGGID(String bggPlayID){
        List<Play> returnMe = Play.find(Play.class, StringUtil.toSQLName("bggPlayID") + " = ?", bggPlayID);
        if (returnMe.isEmpty()){
            return null;
        }else {
            return returnMe.get(0);
        }
    }


    public static List listPlaysNewOld(int sortType){
        String query;
        query = " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                    "Select Max(" + StringUtil.toSQLName("score") + ") " +
                    " from " + StringUtil.toSQLName("PlayersPerPlay") +
                    " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 ";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        query = query + ", P." + StringUtil.toSQLName("ID") + " DESC";
        /*Select dateSort_NewOld = Select.from(Play.class);
        dateSort_NewOld.orderBy(StringUtil.toSQLName("playDate") + " DESC, " + StringUtil.toSQLName("ID") + " DESC");
        return dateSort_NewOld.list();*/
        return Play.findWithQuery(Play.class,query);
    }



    public static List<Play> listPlaysNewOld(String mSearchQuery, boolean allowLike, boolean allowExpansions, int sortType){
        String query;
        if (mSearchQuery.contains("'")) {
            mSearchQuery = mSearchQuery.replaceAll("'", "''");
        }
        if (mSearchQuery.equals("")) {
           return listPlaysNewOld(sortType);
        }else {
           query =  " SELECT P.*, " +
                   "("+
                   "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                   " from " + StringUtil.toSQLName("PlayersPerPlay") +
                   " INNER JOIN " + StringUtil.toSQLName("Player") +
                   " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                   " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                   " and " + StringUtil.toSQLName("score") + " > 0 " +
                   " and " + StringUtil.toSQLName("score") + " = " +
                   " (" +
                   "Select Max(" + StringUtil.toSQLName("score") + ") " +
                   " from " + StringUtil.toSQLName("PlayersPerPlay") +
                   " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                   " )" +
                   ") as " + StringUtil.toSQLName("winners") +
                   " FROM " + StringUtil.toSQLName("Play") + " P " +
                    " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                    " INNER JOIN " + StringUtil.toSQLName("Game") +
                    " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id");
            if (!allowExpansions){
                query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 ";
            }
           if(allowLike){
               query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " LIKE '%" + mSearchQuery + "%'";
           }else {
               query = query + " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " = '" + mSearchQuery + "'";
           }
            switch (sortType) {
                case 0:
                    query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                    break;
                case 1:
                    query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                    break;
                case 2:
                    query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                    break;
                case 3:
                    query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                    break;
            }
            query = query + ", P." + StringUtil.toSQLName("ID") + " DESC";
           //query = query + " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
           return Play.findWithQuery(Play.class,query);
        }
    }

    public static List<Play> listPlaysNewOld_GameGroup(String gameGroup, int sortType){
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("gameGroup") + " = ?";
        //query = query + " order by " + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("playDate") + " DESC, "  + StringUtil.toSQLName("Play") + "." + StringUtil.toSQLName("id") + " DESC";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY P." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY P." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        query = query + ", P." + StringUtil.toSQLName("ID") + " DESC";
        return Play.findWithQuery(Play.class, query, gameGroup);

    }

    public static List<Play> gameTenByTen_GameGroup(GameGroup group, Game game, int year, int sortType){
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " PPGG " +
                " ON P." + StringUtil.toSQLName("id") + " = PPGG." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                " ON P." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("TenByTen") + " TBT " +
                " ON PPGG." + StringUtil.toSQLName("gameGroup") + " = TBT." + StringUtil.toSQLName("gameGroup") +
                " INNER JOIN " + StringUtil.toSQLName("Game") + " G " +
                " ON GPP." + StringUtil.toSQLName("game") + " = G." + StringUtil.toSQLName("id") +
                " WHERE PPGG. " + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND TBT." + StringUtil.toSQLName("game") + " = GPP." + StringUtil.toSQLName("game") +
                " AND G." + StringUtil.toSQLName("id") + " = ? " +
                " AND G." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND STRFTIME('%Y', DATETIME(SUBSTR(P." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                " GROUP BY P." + StringUtil.toSQLName("id");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        query = query + ", P." + StringUtil.toSQLName("ID") + " DESC";
        return Play.findWithQuery(Play.class,query, group.getId().toString(), game.getId().toString(), year + "");
    }

    public static List<Play> totalPlays_TenByTen_GameGroup(GameGroup group, int year, int sortType){
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " PPGG " +
                " ON P." + StringUtil.toSQLName("id") + " = PPGG." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") + " GPP " +
                " ON P." + StringUtil.toSQLName("id") + " = GPP." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("TenByTen") + " TBT " +
                " ON PPGG." + StringUtil.toSQLName("gameGroup") + " = TBT." + StringUtil.toSQLName("gameGroup") +
                " INNER JOIN " + StringUtil.toSQLName("Game") + " G " +
                " ON GPP." + StringUtil.toSQLName("game") + " = G." + StringUtil.toSQLName("id") +
                " WHERE PPGG. " + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND TBT." + StringUtil.toSQLName("game") + " = GPP." + StringUtil.toSQLName("game") +
                " AND G." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND STRFTIME('%Y', DATETIME(SUBSTR(P." + StringUtil.toSQLName("playDate") + ",0, 11), 'unixepoch')) = ? " +
                " GROUP BY P." + StringUtil.toSQLName("id");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY G." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        query = query + ", P." + StringUtil.toSQLName("ID") + " DESC";
        return Play.findWithQuery(Play.class, query, group.getId().toString(), year + "");
    }

    public static List<Play> totalWins_GameGroup_Player(GameGroup group, Player player, int sortType) {
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                " ON P." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND " + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? " +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("score") + " >= " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("playHighScore");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, group.getId().toString(), player.getId().toString());
    }

    public static List<Play> totalPlays_Player(Player player, int sortType) {
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                " ON P." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? ";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, player.getId().toString());
    }

    public static List<Play> totalPlays_Player_GameGroup(Player player, GameGroup group, int sortType) {
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                " ON P." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") + " = " + StringUtil.toSQLName("PlaysPerGameGroup") + "." + StringUtil.toSQLName("play") +
                " AND " + StringUtil.toSQLName("GameGroup") + " = ?";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, player.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalWins_Player(Player player, int sortType) {
        String query;
        query =  " SELECT P.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " P " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") +
                " ON P." + StringUtil.toSQLName("id") + " = " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("play") +
                " AND "+ StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("player") + " = ? " +
                " AND "+ StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("score") + " >= " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("playHighScore");
        switch (sortType) {
            case 0:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY P." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, player.getId().toString());
    }

    public static List<Play> totalAsteriskWins_GameGroup_Player(GameGroup group, Player player, int sortType) {
        String query;
        query =  " SELECT PP.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " PP " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                " ON PP." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                " WHERE P." + StringUtil.toSQLName("player") + " = ? " +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                " AND P." + StringUtil.toSQLName("score") + " > 0 " +
                " AND P." + StringUtil.toSQLName("score") + " = " +
                " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " B " +
                " ON A." + StringUtil.toSQLName("play") + " = B." + StringUtil.toSQLName("play") +
                " WHERE B." + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND A." + StringUtil.toSQLName("player") + " in " +
                " (SELECT " + StringUtil.toSQLName("player") +
                "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                " )" +
                " GROUP BY P." + StringUtil.toSQLName("play") +
                " HAVING " +
                " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " R " +
                " ON Q." + StringUtil.toSQLName("play") + " = R." + StringUtil.toSQLName("play") +
                " WHERE R." + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                ") = 1";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, player.getId().toString(), group.getId().toString(), group.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalAsteriskWins_Player(Player player, int sortType) {
        String query;
        query =  " SELECT PP.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " PP " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                " ON PP." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                " WHERE P." + StringUtil.toSQLName("player") + " = ? " +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                " AND P." + StringUtil.toSQLName("score") + " > 0 " +
                " AND P." + StringUtil.toSQLName("score") + " = " +
                " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " )GROUP BY P." + StringUtil.toSQLName("play") +
                " HAVING " +
                " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                " WHERE Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                ") = 1";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class,query, player.getId().toString());
    }

    public static List<Play> totalGroupLosses(GameGroup group, int sortType) {
        String query;
        query =  " SELECT PP.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " PP " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                " ON PP." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                " WHERE P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND P." + StringUtil.toSQLName("player") + " in " +
                " (SELECT " + StringUtil.toSQLName("player") +
                "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                " GROUP BY P." + StringUtil.toSQLName("play") +
                " HAVING " +
                " (SELECT COUNT(*) " +
                " FROM " +
                " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " R " +
                " ON Q." + StringUtil.toSQLName("play") + " = R." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("PlayersPerGameGroup") + " S " +
                " ON Q." + StringUtil.toSQLName("player") + " = S." + StringUtil.toSQLName("player") +
                " WHERE S." + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                ") = " +
                " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, group.getId().toString(), group.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalGroupLosses(int sortType) {
        String query;
        query =  " SELECT PP.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " PP " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                " ON PP." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                " WHERE P." + StringUtil.toSQLName("score") + " < P." + StringUtil.toSQLName("playHighScore") +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " GROUP BY P." + StringUtil.toSQLName("play") +
                " HAVING " +
                " (SELECT COUNT(*) " +
                " FROM " +
                " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                " WHERE Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                ") = " +
                " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                "  FROM " + StringUtil.toSQLName("Player") +
                " )";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query);
    }

    public static List<Play> totalSharedWins(GameGroup group, int sortType) {
        String query;
        query =  " SELECT PP.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " PP " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                " ON PP." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                " WHERE P." + StringUtil.toSQLName("score") + " != 0" +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND P." + StringUtil.toSQLName("score") + " = " +
                " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") + " ) " +
                " AND P." + StringUtil.toSQLName("player") + " in " +
                " (SELECT " + StringUtil.toSQLName("player") +
                "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)" +
                " GROUP BY P." + StringUtil.toSQLName("play") +
                " HAVING " +
                " (SELECT COUNT(*) " +
                " FROM " +
                " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                "  FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                " INNER JOIN " + StringUtil.toSQLName("PlaysPerGameGroup") + " R " +
                " ON Q." + StringUtil.toSQLName("play") + " = R." + StringUtil.toSQLName("play") +
                " INNER JOIN " + StringUtil.toSQLName("PlayersPerGameGroup") + " S " +
                " ON Q." + StringUtil.toSQLName("player") + " = S." + StringUtil.toSQLName("player") +
                " WHERE S." + StringUtil.toSQLName("GameGroup") + " = ?" +
                " AND Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                ") = " +
                " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                "  FROM " + StringUtil.toSQLName("PlayersPerGameGroup") +
                "  WHERE " + StringUtil.toSQLName("GameGroup") + " = ?)";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query, group.getId().toString(), group.getId().toString(), group.getId().toString());
    }

    public static List<Play> totalSharedWins(int sortType) {
        String query;
        query =  " SELECT PP.*, " +
                "("+
                "Select GROUP_CONCAT(" + StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("playerName") + ", ', ')  " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " INNER JOIN " + StringUtil.toSQLName("Player") +
                " ON " + StringUtil.toSQLName("PlayersPerPlay") + "." + StringUtil.toSQLName("Player") + " = "+ StringUtil.toSQLName("Player") + "." + StringUtil.toSQLName("id") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " and " + StringUtil.toSQLName("score") + " > 0 " +
                " and " + StringUtil.toSQLName("score") + " = " +
                " (" +
                "Select Max(" + StringUtil.toSQLName("score") + ") " +
                " from " + StringUtil.toSQLName("PlayersPerPlay") +
                " where " + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("id") +
                " )" +
                ") as " + StringUtil.toSQLName("winners") +
                " FROM " + StringUtil.toSQLName("Play") + " PP " +
                " INNER JOIN " + StringUtil.toSQLName("GamesPerPlay") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("play") + " = PP." + StringUtil.toSQLName("id") +
                " INNER JOIN " + StringUtil.toSQLName("Game") +
                " ON " + StringUtil.toSQLName("GamesPerPlay") + "." + StringUtil.toSQLName("game") + " = " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("id") +
                " INNER JOIN "+ StringUtil.toSQLName("PlayersPerPlay") + " P " +
                " ON PP." + StringUtil.toSQLName("id") + " = P." + StringUtil.toSQLName("play") +
                " WHERE P." + StringUtil.toSQLName("score") + " != 0" +
                " AND " + StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("expansionFlag") + " = 0 " +
                " AND P." + StringUtil.toSQLName("score") + " = " +
                " (SELECT MAX(A." + StringUtil.toSQLName("score") + ") " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " A " +
                " WHERE A." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") + " ) " +
                " GROUP BY P." + StringUtil.toSQLName("play") +
                " HAVING " +
                " (SELECT COUNT(*) " +
                " FROM " +
                " (SELECT COUNT(Q." + StringUtil.toSQLName("score") + ") " +
                " FROM " + StringUtil.toSQLName("PlayersPerPlay") + " Q " +
                " WHERE Q." + StringUtil.toSQLName("play") + " = P." + StringUtil.toSQLName("play") +
                " AND Q." + StringUtil.toSQLName("score") + " = P." + StringUtil.toSQLName("score") +
                " GROUP BY Q." + StringUtil.toSQLName("player") + ") AS Z " +
                ") = " +
                " (SELECT COUNT(" + StringUtil.toSQLName("player") + ")" +
                "  FROM " + StringUtil.toSQLName("Player") +
                "  )";
        switch (sortType) {
            case 0:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " DESC";
                break;
            case 1:
                query = query + " ORDER BY PP." + StringUtil.toSQLName("playDate") + " ASC";
                break;
            case 2:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " ASC";
                break;
            case 3:
                query = query + " ORDER BY " +StringUtil.toSQLName("Game") + "." + StringUtil.toSQLName("gameName") + " DESC";
                break;
        }
        return Play.findWithQuery(Play.class, query);
    }

}