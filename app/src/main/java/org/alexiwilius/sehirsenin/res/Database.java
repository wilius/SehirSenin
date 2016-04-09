package org.alexiwilius.sehirsenin.res;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IntDef;

import org.alexiwilius.ranti_app.location.Location;
import org.alexiwilius.ranti_app.sqlite.DatabaseManager;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.model.Station;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexiWilius on 4.10.2015.
 */
public class Database {

    public static final int MAX_STARRED_STATION_AMOUNT = 5;
    private static SQLiteDatabase mUser, mCache;

    private static final String CACHE_DB = "cache.db";
    private static final String USER_DB = "sehir_senin.db";

    @IntDef({STARRED_STATION, NORMAL_STATION})
    public @interface StarredStatus {
    }

    public static final int STARRED_STATION = 1;
    public static final int NORMAL_STATION = 0;

    static {
        Activity activity = UIThread.getActivity();
        try {
            boolean updateAvailable = DatabaseManager.updateIfAvailable(activity, CACHE_DB, activity.getString(R.string.db_version));

            mCache = DatabaseManager.getInstance(activity, CACHE_DB, DatabaseManager.READABLE);
            mUser = DatabaseManager.getInstance(activity, USER_DB, DatabaseManager.READABLE);

            createFavoritedStationsTable();
            if (updateAvailable)
                moveFavoritedStationsToCache();

        } catch (IOException e) {
            console.notifyAndClose(activity, e.getMessage());
        }
    }

    public static List<Station> getNearStations(Double lat, Double lng) throws JSONException, IOException {
        Cursor c = mCache.rawQuery("" +
                        " select s.* " +
                        " from station s " +
                        "       left outer join (select @lat lat, @lng lng, 0.005 dif) m on 1 = 1 " +
                        " where (s.lat between m.lat - m.dif and m.lat + m.dif " +
                        "           and s.lng between m.lng - m.dif and m.lng + m.dif) " +
                        "       or favorited = 1 ",
                new String[]{Double.toString(lat), Double.toString(lng)});

        List<Station> result = new ArrayList<>();

        if (c.moveToFirst())
            do {
                result.add(new Station(
                        c.getString(c.getColumnIndex("id")),
                        c.getString(c.getColumnIndex("name")),
                        c.getDouble(c.getColumnIndex("lat")),
                        c.getDouble(c.getColumnIndex("lng")),
                        c.getInt(c.getColumnIndex("favorited")) == 1));
            } while (c.moveToNext());

        return result;
    }

    public static synchronized void changeStationStarredStatus(String stationId, @StarredStatus Integer status) throws ReachedMaxStarredCardException {
            if (STARRED_STATION == status) {
                Cursor c = mUser.rawQuery("select count(*) adet from favorited_station", null);
                if (c.moveToFirst() && c.getInt(c.getColumnIndex("adet")) >= MAX_STARRED_STATION_AMOUNT)
                    throw new ReachedMaxStarredCardException();
            }

            mCache.execSQL("update station set favorited = ? where id = ?", new Object[]{status, stationId});
            mUser.execSQL(
                    STARRED_STATION == status ?
                            "insert into favorited_station(station_id) values(?)" :
                            "delete from favorited_station where station_id = ?", new Object[]{stationId});
    }

    public static void addCard(String number, String name) throws Exception {
        Cursor c = mUser.rawQuery("" +
                " select (select " +
                "           count(*) " +
                "         from card where name = ?) name," +
                "        (select " +
                "           count(*) " +
                "         from card where number = ? ) number", new String[]{name, number});
        c.moveToFirst();
        if (c.getInt(c.getColumnIndex("number")) > 0)
            throw new Exception(String.format(UIThread.getActivity().getString(R.string.card_number_exists), number));
        if (c.getInt(c.getColumnIndex("name")) > 0)
            throw new Exception(String.format(UIThread.getActivity().getString(R.string.card_name_exists), name));
        c.close();

        mUser.execSQL("insert into card values(?, ?, case when exists(select * from card where starred = 1) then 0 else 1 end)", new String[]{number, name});
    }

    public static void markCardStarred(String cardNumber) {
        mUser.execSQL("update card set starred = case when number = ? then 1  else 0 end", new String[]{cardNumber});
    }
    
    public static JSONArray getCardList() {
        return query(mUser, "select * from card", new String[]{});
    }

    public static JSONObject getStarredCard() {
        JSONArray res = query(mUser, "select * from card where starred = 1", new String[]{});
        if (res.length() == 0) return null;
        try {
            return res.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
            console.showErrorMessage(UIThread.getActivity(), e.getMessage());
            return null;
        }
    }

    public static void deleteCard(String number) {
        mUser.execSQL("delete from card where number = ?", new String[]{number});
        mUser.execSQL("" +
                " update card set starred = 1 " +
                " where number = (select number from card order by rowid asc limit 1) " +
                " and not exists(select * from card where starred = 1)");
    }

    public static JSONArray getLineList(String... idList) {
        if (idList.length == 0)
            return new JSONArray();

        StringBuilder builder = new StringBuilder(2 * idList.length - 1);
        builder.append("?");
        for (int i = 1; i < idList.length; i++)
            builder.append(",?");

        return query(mCache, String.format("select * from line where id in (%s) order by id", builder.toString()), idList);
    }

    public static JSONArray findLine(String query) {
        if (query == null)
            query = "";
        return query(mCache, "" +
                        " select l.id, l.id||' - '||l.name name " +
                        " from line l " +
                        " where " +
                        " lower(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(\n" +
                        " replace(l.id||' '||l.name, '\u0130', 'i'), 'I', 'i'), '\u00c7', 'c'), '\u015e', 's'), '\u00dc', 'u'), '\u00d6', 'o') " +
                        " , '\u00f6', 'o'), '\u00fc', 'u'), '\u015f', 's'), '\u00c7', 'c'), '\u0131', 'i'), '\u011e', 'g'), '\u011f', 'g')) like " +
                        " lower(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(\n" +
                        " replace(?, '\u0130', 'i'), 'I', 'i'), '\u00c7', 'c'), '\u015e', 's'), '\u00dc', 'u'), '\u00d6', 'o') " +
                        " , '\u00f6', 'o'), '\u00fc', 'u'), '\u015f', 's'), '\u00c7', 'c'), '\u0131', 'i'), '\u011e', 'g'), '\u011f', 'g')) order by id limit 7 ",
                new String[]{"%" + query.trim().replaceAll("\\s+", "*") + "%"});
    }

    public static JSONObject getLine(String line) {
        try {
            return query(mCache, " select * from line where id = ? ", new String[]{line}).getJSONObject(0);
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONArray query(SQLiteDatabase instance, String sql, String[] args) {
        JSONArray result = new JSONArray();
        Cursor c = instance.rawQuery(sql, args);
        try {
            if (c.moveToFirst()) {
                JSONObject object;
                do {
                    object = new JSONObject();
                    for (String column : c.getColumnNames())
                        object.put(column, c.getString(c.getColumnIndex(column)));

                    result.put(object);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            console.showErrorMessage(UIThread.getActivity(), e.getMessage());
        } finally {
            c.close();
        }
        return result;
    }

    private static void createFavoritedStationsTable() {
        try {
            mUser.execSQL("" +
                    " CREATE TABLE favorited_station ( " +
                    "   station_id INTEGER NOT NULL, " +
                    "   PRIMARY KEY(station_id) " +
                    " );");
        } catch (Exception e) {
        }
    }

    private static boolean moveFavoritedStationsToCache() {
        try {
            Cursor c = mUser.rawQuery("select * from favorited_station", null);
            if (c.moveToFirst()) {
                do {
                    mCache.execSQL("update station set favorited = ? where id = ?", new Object[]{STARRED_STATION, c.getString(c.getColumnIndex("station_id"))});
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            console.showErrorMessage(UIThread.getActivity(), e.getMessage());
            return false;
        }
        return true;
    }
}
