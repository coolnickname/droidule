package nl.yildri.droidule;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import nl.yildri.droidule.Theming.ThemeManager;
import nl.yildri.droidule.Util.DatabaseOpenHelper;

public class Droidule extends Application {
    private static Context context;
    private static SQLiteDatabase writableDatabase;

    public static Context getContext() {
        return Droidule.context;
    }

    public static SQLiteDatabase getWritableDatabase() {
        return Droidule.writableDatabase;
    }

    public void onCreate() {
        super.onCreate();
        Droidule.context = getApplicationContext();
        Droidule.writableDatabase = new DatabaseOpenHelper(Droidule.getContext()).getWritableDatabase();

        //Update theme
        setTheme(ThemeManager.getTheme());
    }
}
