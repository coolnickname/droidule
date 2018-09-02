package nl.yildri.droidule.Xedule;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import nl.yildri.droidule.Droidule;
import nl.yildri.droidule.Schedule.Event;
import nl.yildri.droidule.Util.SQLCreatorUtil;
import nl.yildri.droidule.Xedule.api.XeduleAPI;

public class Xedule {
    private static int cacheTimeout = 60; // in seconds
    private static boolean cacheEnabled = false;

    public static JSONArray getArray(String location) throws JSONException {
        return new JSONArray(get(location));
    }

    public static JSONObject getObject(String location) throws JSONException {
        return new JSONObject(get(location));
    }

    private static String get(String location) {
        File cacheFile = new File(Droidule.getContext().getCacheDir(), location);
        String output = null;

        if (cacheEnabled && cacheFile.exists()) {
            try {
                BufferedReader cacheReader = new BufferedReader(new FileReader(cacheFile));
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = cacheReader.readLine()) != null) sb.append(line + "\n");

                cacheReader.close();

                if (sb.length() > 0) {
                    output = sb.toString();
                }
            } catch (Exception e) {
                Log.d("Xedule", "aa", e); // Do nothing
            }

        } else if (!cacheFile.exists()) {
            if (!cacheFile.getParentFile().exists()) {
                cacheFile.getParentFile().mkdirs();
            }
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (output == null) try {
            FileWriter cacheWriter = new FileWriter(cacheFile);
            cacheWriter.write(output);
            cacheWriter.close();
        } catch (IOException e) {
            Log.w("Xedule", "Could not write cache file", e);
        }

        return output;
    }

    public static void updateOrganisations() {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        db.beginTransaction();

        ArrayList<Organisation> organisations = XeduleAPI.getOrganisations();

        try {

            for (Organisation organisation : organisations) {
                organisation.save(db);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.endTransaction();
    }

    public static void updateLocations(Organisation organisation) {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        db.beginTransaction();

        ArrayList<Location> locations = XeduleAPI.getLocations(organisation);

        if (locations == null) {
            Log.w("Xedule", "Could not obtain locations.");
            return;
        }

        try {

            for (Location location : locations) {
                location.save(db);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.endTransaction();
    }

    public static void updateAttendees(Location location, ProgressBar progressBar) {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        db.beginTransaction();

        try {
            ArrayList<Attendee> attendees = XeduleAPI.getAttendees(location);

            for (Attendee attendee : attendees) {
                attendee.save(db);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void updateEvents(Attendee attendee, int year, int week) {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        SQLCreatorUtil.createWeekscheduleAgeTable(db);
        SQLCreatorUtil.createAttendeeEventsView(db);

        db.beginTransaction();
        db.delete("attendee_events_view", "attendee = ? AND year = ? AND week = ?",
                new String[]{String.valueOf(attendee.getId()), String.valueOf(year), String.valueOf(week)});

        try {
            ArrayList<Event> events = XeduleAPI.getEvents(attendee, year, week);

            for (Event event : events) {
                event.save(db);
            }

            //TODO: Make the SQL query replace old entries instead of deleting all the old ones before adding a new one.
            //TODO: Make this not delete entries it shouldn't. Currently overpopulating the DB seems to work for caching.
            /*db.execSQL("DELETE FROM weekschedule_age\n" +
                    "WHERE EXISTS (\n" +
                    "  SELECT *\n" +
                    "  FROM weekschedule_age\n" +
                    "  WHERE weekschedule_age.attendee = " + attendee.getId() + "\n" +
                    "  AND weekschedule_age.year = " + year + "\n" +
                    "  AND weekschedule_age.week = " + week + "\n" +
                    ")");*/

            ContentValues values = new ContentValues();
            values.put("attendee", attendee.getId());
            values.put("year", year);
            values.put("week", week);
            values.put("lastUpdate", System.currentTimeMillis() / 1000L);
            db.insertWithOnConflict("weekschedule_age", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Xedule", "Couldn't update events for attendee #" + attendee.getId(), e);
        } finally {
            db.endTransaction();
        }
    }

}
