package nl.yildri.droidule.Xedule;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import nl.yildri.droidule.Util.DatabaseOpenHelper;
import nl.yildri.droidule.Droidule;
import nl.yildri.droidule.Util.SQLCreatorUtil;

public class Location implements Comparable<Location>
{
    private int id;
    private String name;
    private Organisation organisation;
    private String yearId = null;

    public Location(int id)
    {
        this.id = id;
    }

    public Location(int id, String name, Organisation organisation)
    {
        this.id = id;
        this.name = name;
        this.organisation = organisation;
    }

    public Location(Cursor cursor)
    {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
        this.organisation = new Organisation(cursor.getInt(2));
    }

    public String getYearId(){
        if(yearId == null){
            yearId = id + "_" + Calendar.getInstance().get(Calendar.YEAR);
        }

        return yearId;
    }

    public boolean populate()
    {
        SQLiteDatabase db = Droidule.getWritableDatabase();
        Cursor cursor = db.query("locations", new String[]{ "id", "name", "organisation" }, "id = " + this.id, null, null, null, "id", null);

        if (cursor == null || cursor.getCount() == 0) return false;

        cursor.moveToFirst();
        name = cursor.getString(1);
        organisation = new Organisation(cursor.getInt(2));

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

    public Organisation getOrganisation()
    {
        if (organisation == null) populate();

        return organisation;
    }

    public String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(Location loc)
    {
        return this.name.compareTo(loc.name);
    }

    public void save(SQLiteDatabase db) {
        SQLCreatorUtil.createLocationsTable(db);

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("organisation", organisation.getId());

        db.insertWithOnConflict("locations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void save()
    {
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getWritableDatabase();
        save(db);
        db.close();
    }

    public ArrayList<Attendee> getAttendees()
    {
        ArrayList<Attendee> output = new ArrayList<Attendee>();
        SQLiteDatabase db = new DatabaseOpenHelper(Droidule.getContext()).getReadableDatabase();

        try {
            Cursor cursor = db.query("attendees", new String[]{ "id", "name", "location", "type" }, "location = " + this.id, null, null, null, "name", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                output.add(new Attendee(cursor));
                cursor.moveToNext();
            }

            db.close();

        }catch(SQLiteException e){

        }

        return output;
    }
}
