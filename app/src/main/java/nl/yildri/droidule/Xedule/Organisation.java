package nl.yildri.droidule.Xedule;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import nl.yildri.droidule.Droidule;
import nl.yildri.droidule.Util.DatabaseOpenHelper;
import nl.yildri.droidule.Util.SQLCreatorUtil;
import nl.yildri.droidule.Xedule.api.XeduleAPI;

public class Organisation implements Comparable<Organisation> {
    private int id;
    private String name = null;
    private URL url;
    private int compatible = -1; //-1=null, 0=incompatible, 1=droidule compatible, 2=xedroid compatible
    private String userCookie = null;

    public Organisation(int id) {
        this.id = id;
    }

    public Organisation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Organisation(Cursor cursor) {
        id = cursor.getInt(0);
        name = cursor.getString(1);
    }

    public static ArrayList<Organisation> getAll() {
        ArrayList<Organisation> output = new ArrayList<Organisation>();
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getReadableDatabase();

        try {
            Cursor cursor = db.query("organisations", new String[]{"id", "name"}, null, null, null, null, "name", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                output.add(new Organisation(cursor));
                cursor.moveToNext();
            }

            db.close();

        } catch (SQLiteException e) {

        }

        return output;
    }

    public URL getURL() {
        if (url == null) updateURL();

        return url;
    }

    public int getCompatible() {
        if (compatible == -1) updateURL();

        return compatible;
    }

    private void updateURL() { //Should be async but causes the URL to be null when we need it.
        try {
            URL queryURL = new URL("https://roosters.xedule.nl/Organisatie/OrganisatorischeEenheid/" + id);

            HttpURLConnection urlConnection = (HttpURLConnection) queryURL.openConnection();

            urlConnection.getResponseCode(); //Dont remove this or de redirect doesnt work (?)
            url = urlConnection.getURL();


            if (url.toString().contains("https://roosters.xedule.nl/")) {
                compatible = 2; //xedroid compatible

            } else if (url.toString().contains("https://sa") && url.toString().contains(".xedule.nl/")) {
                compatible = 1; //droidule compatible

                url = new URL(url.toString().split(Pattern.quote("/?"), 2)[0]);

            } else {
                compatible = 0; //incompatible (login required most likely)

            }

            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserCookie() {
        if (userCookie == null) {
            userCookie = XeduleAPI.getUserCookie(getURL());
        }

        return userCookie;
    }

    public boolean populate() {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        Cursor cursor = db.query("organisations", new String[]{"id", "name"}, "id = " + id, null, null, null, "id", null);

        if (cursor == null || cursor.getCount() == 0) return false;

        cursor.moveToFirst();
        name = cursor.getString(1);

        return true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        if (name == null) populate();

        return name;
    }

    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Organisation org) {
        return getName().compareTo(org.getName());
    }

    public void save(SQLiteDatabase db) {
        SQLCreatorUtil.createOrganisationsTable(db);

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);

        db.insertWithOnConflict("organisations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void save() {
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public ArrayList<Location> getLocations() {
        ArrayList<Location> output = new ArrayList<Location>();
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getReadableDatabase();

        try {
            Cursor cursor = db.query("locations", new String[]{"id", "name", "organisation"}, "organisation = " + id, null, null, null, "name", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                output.add(new Location(cursor));
                cursor.moveToNext();
            }

            db.close();

        } catch (SQLiteException e) {

        }

        return output;
    }
}
