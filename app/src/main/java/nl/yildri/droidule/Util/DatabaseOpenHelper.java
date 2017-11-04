package nl.yildri.droidule.Util;

import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "droidule";
    public static final int DATABASE_VERSION = 1;

    private Context context;

    public DatabaseOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            //Reads from init_sql file, which didn't seem to work and won't be working anytime soon
            // since I edited the file.
            /*InputStream inputStream = context.getResources().openRawResource(R.raw.init_sql);
            byte[] reader = new byte[inputStream.available()];
            while (inputStream.read(reader) != -1) {}

            String[] queries = new String(reader).split("\n-\n");
            for (String query : queries) db.execSQL(query);*/


            //Executes the SQL I extracted from init_sql. Doesn't seem to work however. Does it
            // even get called?
            /*db.execSQL("CREATE TABLE IF NOT EXISTS organisations (\n" +
                    "    id             INT,\n" +
                    "    name           TEXT,\n" +
                    "\n" +
                    "    PRIMARY KEY (id)\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS locations (\n" +
                    "    id             INT,\n" +
                    "    name           TEXT,\n" +
                    "    organisation   INT,\n" +
                    "\n" +
                    "    PRIMARY KEY (id),\n" +
                    "    FOREIGN KEY (organisation) REFERENCES organisations\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS attendees (\n" +
                    "    id             INT,\n" +
                    "    name           TEXT,\n" +
                    "    location       INT,\n" +
                    "    type           INT,\n" +
                    "\n" +
                    "    PRIMARY KEY (id),\n" +
                    "    FOREIGN KEY (location) REFERENCES locations\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS events (\n" +
                    "    id             INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    year           INT,\n" +
                    "    week           INT,\n" +
                    "    day            INT,\n" +
                    "    description    TEXT,\n" +
                    "    start          TEXT,\n" +
                    "    end            TEXT\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS attendee_events (\n" +
                    "    attendee       INT,\n" +
                    "    event          INT,\n" +
                    "\n" +
                    "    FOREIGN KEY (attendee) REFERENCES attendees,\n" +
                    "    FOREIGN KEY (event) REFERENCES events\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS weekschedule_age (\n" +
                    "    attendee       INT,\n" +
                    "    year           INT,\n" +
                    "    week           INT,\n" +
                    "    lastUpdate     INT,\n" +
                    "\n" +
                    "    FOREIGN KEY (attendee) REFERENCES attendees\n" +
                    ");\n" +
                    "\n" +
                    "CREATE VIEW IF NOT EXISTS attendee_events_view AS\n" +
                    "    SELECT attendee, event, year, week, day, description, start, end, name, location, type\n" +
                    "    FROM attendee_events\n" +
                    "        INNER JOIN events ON attendee_events.event = events.id\n" +
                    "        INNER JOIN attendees ON attendee_events.attendee = attendees.id;\n" +
                    "\n" +
                    "CREATE TRIGGER IF NOT EXISTS attendee_events_remove\n" +
                    "INSTEAD OF DELETE ON attendee_events_view\n" +
                    "BEGIN\n" +
                    "    DELETE FROM attendee_events WHERE attendee = old.attendee AND event = old.event;\n" +
                    "    DELETE FROM events WHERE id = old.event;\n" +
                    "END;");*/


            //This can only be initialised here, however it doesn't seem to get used anywhere.
            //SQLCreatorUtil.createAttendeeEventsRemoveTrigger(db);
            //I moved it to createAttendeeEventsView() because it's used together. Apparently this
            // method doesn't get called. >:/

        }
        catch (Exception e)
        {
            Log.e("Droidule", "Could not create database.", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
