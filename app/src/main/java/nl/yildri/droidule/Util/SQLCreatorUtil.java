package nl.yildri.droidule.Util;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Yildri on 03/11/2017.
 */

public class SQLCreatorUtil {
    //This does not make init_sql in /res/raw/ obsolete, as it can be used as a reference.
    //But it doesn't get executed in the code anymore so it's kinda obsolete-ish.

    //Also sometimes tables rely on other tables so sometimes a method in this class calls one of
    // it's peers.

    /* Tables */

    public static void createOrganisationsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS organisations (\n" +
                "id INT,\n" +
                "name TEXT,\n" +
                "PRIMARY KEY (id)\n" +
                ");");
    }

    public static void createLocationsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (\n" +
                "id INT,\n" +
                "name TEXT,\n" +
                "organisation INT,\n" +
                "PRIMARY KEY (id),\n" +
                "FOREIGN KEY (organisation) REFERENCES organisations\n" +
                ");");
    }

    public static void createAttendeesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS attendees (\n" +
                "id INT,\n" +
                "name TEXT,\n" +
                "location INT,\n" +
                "type INT,\n" +
                "PRIMARY KEY (id),\n" +
                "FOREIGN KEY (location) REFERENCES locations\n" +
                ");");
    }

    public static void createEventsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS events (\n" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "year INT,\n" +
                "week INT,\n" +
                "day INT,\n" +
                "description TEXT,\n" +
                "start TEXT,\n" +
                "end TEXT\n" +
                ");");
    }

    public static void createAttendeeEventsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS attendee_events (\n" +
                "attendee INT,\n" +
                "event INT,\n" +
                "FOREIGN KEY (attendee) REFERENCES attendees,\n" +
                "FOREIGN KEY (event) REFERENCES events\n" +
                ");");
        createEventsTable(db);
    }

    public static void createWeekscheduleAgeTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS weekschedule_age (\n" +
                "attendee INT,\n" +
                "year INT,\n" +
                "week INT,\n" +
                "lastUpdate INT,\n" +
                "FOREIGN KEY (attendee) REFERENCES attendees\n" +
                ");");
    }

    /* Views */

    public static void createAttendeeEventsView(SQLiteDatabase db) {
        db.execSQL("CREATE VIEW IF NOT EXISTS attendee_events_view AS\n" +
                "    SELECT attendee, event, year, week, day, description, start, end, name, location, type\n" +
                "    FROM attendee_events\n" +
                "        INNER JOIN events ON attendee_events.event = events.id\n" +
                "        INNER JOIN attendees ON attendee_events.attendee = attendees.id;");
        createAttendeeEventsTable(db);
        createAttendeeEventsRemoveTrigger(db);
    }

    /* Triggers */

    public static void createAttendeeEventsRemoveTrigger(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER IF NOT EXISTS attendee_events_remove\n" +
                "INSTEAD OF DELETE ON attendee_events_view\n" +
                "BEGIN\n" +
                "    DELETE FROM attendee_events WHERE attendee = old.attendee AND event = old.event;\n" +
                "    DELETE FROM events WHERE id = old.event;\n" +
                "END;");
    }

}
