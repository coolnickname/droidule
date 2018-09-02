package nl.yildri.droidule.Xedule;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import nl.yildri.droidule.Util.DatabaseOpenHelper;
import nl.yildri.droidule.Droidule;
import nl.yildri.droidule.Schedule.Event;
import nl.yildri.droidule.R;
import nl.yildri.droidule.Util.SQLCreatorUtil;

public class Attendee implements Comparable<Attendee>
{
    private int id;
    private String name;
    private Location location;
    private Type type;

    public enum Type
    {
        CLASS    (1, R.string.attendee_type_class),
        STAFF    (2, R.string.attendee_type_staff),
        FACILITY (3, R.string.attendee_type_facility);

        public final int id;
        public final int label;

        private Type(int id, int label)
        {
            this.id = id;
            this.label = label;
        }

        public static Type getById(int id)
        {
            switch (id)
            {
                case 1:  return Type.CLASS;
                case 2:  return Type.STAFF;
                case 3:  return Type.FACILITY;
                default: return Type.CLASS;
            }
        }

        public String getName()
        {
            switch (this)
            {
                case CLASS:    return "Klas";
                case STAFF:    return "Medewerker";
                case FACILITY: return "Lokaal";
                default:       return "idk";
            }
        }
    }

    public Attendee(int id)
    {
        this.id = id;
    }

    public Attendee(String name, SQLiteDatabase db)
    {
        this.name = name;

        Cursor cursor = db.query("attendees", new String[]{ "id", "name", "location", "type" }, "name = ?", new String[]{ this.name }, null, null, "id", null);

        cursor.moveToFirst();
        this.id = cursor.getInt(0);
        this.location = new Location(cursor.getInt(2));
        this.type = Type.getById(cursor.getInt(3));
    }

    public Attendee(int id, String name, Location location, String type)
    {
        this.id = id;
        this.name = name;
        this.location = location;

        try
        {
            this.type = Type.valueOf(type.toUpperCase());
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error: " + e.getMessage());
        }
    }

    public Attendee(int id, String name, Location location, Type type)
    {
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
    }

    public Attendee(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
        this.location = new Location(cursor.getInt(2));

        try
        {
            this.type = Type.getById(cursor.getInt(3));
        }
        catch (Exception e)
        {
            Log.e("Xedule", "Error: " + e.getMessage());
        }
    }

    public boolean populate()
    {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        Cursor cursor = db.query("attendees", new String[]{ "id", "name", "location", "type" }, "id = " + this.id, null, null, null, "id", null);

        if (cursor == null || cursor.getCount() == 0) return false;

        cursor.moveToFirst();
        this.name = cursor.getString(1);
        this.location = new Location(cursor.getInt(2));
        this.type = Type.getById(cursor.getInt(3));

        return true;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        if (name == null) populate();

        return name;
    }

    public Location getLocation()
    {
        if (location == null) populate();

        return location;
    }

    public Type getType()
    {
        if (type == null) populate();

        return type;
    }

    public String toString()
    {
        return getName();
    }

    public int getWeekScheduleAge(int year, int week)
    {
        int output = 0;
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getReadableDatabase();

        try {
            Cursor cursor = db.query("weekschedule_age",
                    new String[]{ "lastUpdate" }, "attendee = ? AND year = ? AND week = ?",
                    new String[]{ String.valueOf(id), String.valueOf(year), String.valueOf(week) }, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                output = cursor.getInt(0);
            }

            db.close();

        }catch(SQLiteException e){

        }
        return output;
    }

    @Override
    public int compareTo(Attendee att)
    {
        return this.name.compareTo(att.name);
    }

    public void save(SQLiteDatabase db) {
        SQLCreatorUtil.createAttendeesTable(db);

        ContentValues values = new ContentValues();
        values.put("id", this.id);
        values.put("name", this.name);
        values.put("location", this.location.getId());
        values.put("type", this.type.id);

        db.insertWithOnConflict("attendees", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void save()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public ArrayList<Event> getEvents(int year, int week)
    {
        ArrayList<Event> output = new ArrayList<Event>();
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getReadableDatabase();

        try {
            Cursor cursor = db.query("attendee_events_view",
                    new String[]{ "event", "year", "week", "day", "start", "end", "description" }, "attendee = ? AND year = ? AND week = ?",
                    new String[]{ String.valueOf(id), String.valueOf(year), String.valueOf(week) }, null, null, "event", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                output.add(new Event(cursor));
                cursor.moveToNext();
            }

            db.close();

        }catch(SQLiteException e){

        }

        return output;
    }

    public ArrayList<Event> getEvents(int year, int week, int day)
    {
        ArrayList<Event> output = new ArrayList<Event>();
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getReadableDatabase();

        try {
            Cursor cursor = db.query("attendee_events_view",
                    new String[]{ "event", "year", "week", "day", "start", "end", "description" }, "attendee = ? AND year = ? AND week = ? AND day = ?",
                    new String[]{ String.valueOf(id), String.valueOf(year), String.valueOf(week), String.valueOf(day) }, null, null, "event", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                output.add(new Event(cursor));
                cursor.moveToNext();
            }

            db.close();

        }catch(SQLiteException e){

        }

        return output;
    }
}
