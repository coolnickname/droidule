package nl.yildri.droidule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import nl.yildri.droidule.Theming.ThemeManager;
import nl.yildri.droidule.Util.MiscUtil;
import nl.yildri.droidule.Schedule.DayScheduleFragment;
import nl.yildri.droidule.Schedule.Event;
import nl.yildri.droidule.Schedule.WeekScheduleFragment;
import nl.yildri.droidule.Xedule.Attendee;
import nl.yildri.droidule.Xedule.Xedule;

public class ScheduleActivity extends ActionBarActivity implements WeekScheduleFragment.OnEventSelectedListener,
                                                                   ListView.OnItemClickListener,
                                                                   ListView.OnScrollListener
{
    public final static int RECENTS_LIST_MAX_SIZE = 5;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerAdapter drawerAdapter;
    private WeekScheduleFragment weekScheduleFragment;
    private LinearLayout drawerFooter;

    private Attendee attendee;
    private int year;
    private int week;
    private int weekday;

    //This is so we can show next week's schedule during the weekend without changing the date in the date picker.
    private int shownYear;
    private int shownWeek;

    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        //Update theme
        setTheme(ThemeManager.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);

        Intent intent = getIntent();
        attendee = new Attendee(intent.getIntExtra("attendeeId", 0));
        year = intent.getIntExtra("year", 1970);
        week = intent.getIntExtra("week", 1);
        weekday = intent.getIntExtra("weekday", 1);

        if (attendee.getId() == 0)
        {
            SharedPreferences sharedPref = getSharedPreferences("global", Context.MODE_PRIVATE);
            attendee = new Attendee(sharedPref.getInt("myschedule", 0));
        }

        if (attendee.getId() == 0)
        {
            try
            {
                Intent newIntent = new Intent(this, ClassSelectionActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newIntent);
                finish();
            }
            catch(Exception e)
            {
                Log.e("Droidule", "Error: " + e.getMessage());
            }

            return;
        }

        SharedPreferences sharedPref = getSharedPreferences("global", Context.MODE_PRIVATE);
        ArrayList<String> recents = new ArrayList<String>(Arrays.asList(sharedPref.getString("recents", "").split(",")));
        recents.remove("");
        recents.remove(Integer.toString(attendee.getId()));
        recents.add(0, Integer.toString(attendee.getId()));

        while (recents.size() > RECENTS_LIST_MAX_SIZE) recents.remove(RECENTS_LIST_MAX_SIZE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("recents", TextUtils.join(",", recents.toArray()));
        editor.apply();

        if (year == 1970 && week == 1)
        {
            Calendar calendar = Calendar.getInstance(new Locale("nl", "NL"));
            year = calendar.get(Calendar.YEAR);
            week = calendar.get(Calendar.WEEK_OF_YEAR);
            weekday = calendar.get(Calendar.DAY_OF_WEEK);
        }

        weekScheduleFragment = new WeekScheduleFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.schedule_fragment, weekScheduleFragment).commit();

        updateActionBarTitle();
        refresh(false);

        ListView drawer = (ListView) findViewById(R.id.schedule_drawer);
        drawerAdapter = new DrawerAdapter(this);
        drawer.setAdapter(drawerAdapter);
        drawer.setOnItemClickListener(this);
        drawer.setOnScrollListener(this);

        drawerFooter = (LinearLayout) findViewById(R.id.schedule_drawer_footer);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    public void onItemClick(AdapterView parent, View view, int position, long id)
    {
        drawerAdapter.getItem(position).onClick(parent.getContext());
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return; // TODO: 'Fake' shadow if pre-lollipop?

        if (firstVisibleItem + visibleItemCount != totalItemCount) return; // We're not anywhere near the bottom

        View bottomView = view.getChildAt(view.getChildCount() - 1);
        if (bottomView == null) return; // Shouldn't happen, but still

        int offset = (bottomView.getBottom() - (view.getHeight() - (int) MiscUtil.getPx(8, getResources())));
        drawerFooter.setElevation(Math.min(offset, MiscUtil.getPx(16, getResources())));
    }

    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        // stub
    }

    public void onEventSelected(Event event)
    {
        DayScheduleFragment dayScheduleFragment = new DayScheduleFragment();

        Bundle args = new Bundle();
        args.putInt("attendeeId", attendee.getId());
        args.putInt("year", shownYear);
        args.putInt("week", shownWeek);
        args.putInt("day", event.getDay());
        args.putInt("eventId", event.getId());
        dayScheduleFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .add(R.id.schedule_fragment, dayScheduleFragment)
            .addToBackStack(null)
            .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //This seems to not work. Sad.
        //Update theme
        setTheme(ThemeManager.getTheme());

        drawerAdapter.refresh();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    public Attendee getAttendee()
    {
        return attendee;
    }

    public Calendar getDate()
    {
        Calendar c = Calendar.getInstance(new Locale("nl", "NL"));
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.DAY_OF_WEEK, weekday);

        return c;
    }

    public void setDate(Calendar c)
    {
        year = c.get(Calendar.YEAR);
        week = c.get(Calendar.WEEK_OF_YEAR);
        weekday = c.get(Calendar.DAY_OF_WEEK);

        updateActionBarTitle();
    }

    private void updateActionBarTitle()
    {
        ActionBar bar = getSupportActionBar();
        bar.setTitle(attendee.getName());
        bar.setSubtitle("Week " + week);
    }

    public void refresh(final boolean force)
    {
        if (refreshing) return;
        refreshing = true;

        //TODO: Find a better way to show the schedule of next week during the weekend, without changing
        // the date picker position.

        this.shownWeek = week;
        this.shownYear = year;

        //Weeks start at sunday. 7 = saturday and 1 = sunday.
        if(weekday == 7 || weekday == 1){
            //Set the schedule to a week later
            shownWeek++;

            //If next week is in the next year, let it be in the next year
            if(shownWeek >= 52){
                shownYear++;
                shownWeek = 1;
            }
        }

        weekScheduleFragment.setWeek(shownYear, shownWeek);

        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground(Void... _)
            {
                try
                {
                    Looper.prepare();
                    new Handler();
                }
                catch (Exception e)
                {
                    // TODO: Investigate (Lollipop needs a looper for whatever reason)
                }

                if (force || attendee.getWeekScheduleAge(shownYear, shownWeek) == 0)
                {
                    Xedule.updateEvents(attendee, shownYear, shownWeek);
                    Xedule.updateLocations(attendee.getLocation().getOrganisation());
                }

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        weekScheduleFragment.setEvents(attendee.getEvents(shownYear, shownWeek));
                    }
                });

                return null;
            }

            protected void onPostExecute(Void _)
            {
                refreshing = false;
                weekScheduleFragment.setRefreshing(false);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weekschedule, menu);

        SharedPreferences sharedPref = getSharedPreferences("global", Context.MODE_PRIVATE);
        boolean isMine = sharedPref.getInt("myschedule", 0) == attendee.getId();
        MenuItem item = menu.findItem(R.id.myweekschedule_home);
        item.setChecked(isMine);
        item.setIcon(isMine ? R.drawable.ic_home_white_24dp : R.drawable.ic_home_outline_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        int id = item.getItemId();

        if (id == R.id.weekschedule_weekselect)
        {
            showDatePickerDialog();

            return true;
        }

        if (id == R.id.myweekschedule_home)
        {
            SharedPreferences sharedPref = getSharedPreferences("global", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            if (!item.isChecked())
            {
                editor.putInt("myschedule", attendee.getId());

                item.setChecked(true);
                item.setIcon(R.drawable.ic_home_white_24dp);
            }
            else
            {
                editor.remove("myschedule");

                item.setChecked(false);
                item.setIcon(R.drawable.ic_home_outline_white_24dp);
            }

            editor.apply();

            drawerAdapter.refresh();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettings(View v)
    {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void showDatePickerDialog()
    {
        DialogFragment dialog = new DatePickerFragment();
        dialog.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
        private ScheduleActivity activity;

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);

            this.activity = (ScheduleActivity) context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            // Calculate default selected date
            Calendar c = activity.getDate();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Get dialog's DatePicker
            //TODO: Disable weekends in the picker and make it show the next week if a weekend is
            // the current date. Probably with https://github.com/wdullaer/MaterialDateTimePicker
            // For now let's just focus on showing the next week during the weekend.
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            DatePicker picker = dialog.getDatePicker();

            // String[] weeks = activity.getAttendee().getLocation().getWeeks();

            // // Calculate minimum date
            // // [ "2014/35", "2014/36", ... ] -> [ "2014", "35" ]
            // String[] firstWeek = TextUtils.split(weeks[0], "/");
            // Calendar min = (Calendar) c.clone();
            // min.clear();
            // min.set(Calendar.YEAR, Integer.parseInt(firstWeek[0]));
            // min.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(firstWeek[1]));

            // // Calculate maximum date
            // String[] lastWeek = TextUtils.split(weeks[weeks.length - 1], "/");
            // Calendar max = (Calendar) c.clone();
            // max.clear();
            // max.set(Calendar.YEAR, Integer.parseInt(lastWeek[0]));
            // max.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(lastWeek[1]));
            // max.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

            // // Configure picker
            // picker.setMinDate(min.getTimeInMillis());
            // picker.setMaxDate(max.getTimeInMillis());

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day)
        {
            Calendar c = Calendar.getInstance(new Locale("nl", "NL"));
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);

            activity.setDate(c);
            activity.refresh(false);
        }
    }

    public static class DrawerAdapter extends BaseAdapter
    {
        private Activity activity;
        private ArrayList<Item> items;
        private LayoutInflater inflater;

        public final static int TYPE_HEADER_ITEM = 0;
        public final static int TYPE_LIST_ITEM = 1;
        public final static int TYPE_COUNT = 2;

        public DrawerAdapter(Activity activity)
        {
            this.activity = activity;
            refresh();

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void refresh()
        {
            items = new ArrayList<Item>();

            // Class selection
            items.add(new IntentItem(R.drawable.ic_list_black_24dp, activity.getString(R.string.pick_schedule), ClassSelectionActivity.class));

            // Starred schedule
            SharedPreferences sharedPref = activity.getSharedPreferences("global", Context.MODE_PRIVATE);
            if (sharedPref.getInt("myschedule", 0) != 0)
            {
                Attendee myAttendee = new Attendee(sharedPref.getInt("myschedule", 0));

                items.add(new HeaderItem(activity.getString(R.string.myschedule_label)));
                items.add(new AttendeeItem(myAttendee));
            }

            // Recent schedules
            List<String> recents = Arrays.asList(sharedPref.getString("recents", "").split(","));
            items.add(new HeaderItem(activity.getString(R.string.recent_schedules)));
            for (String recent : recents) try
            {
                items.add(new AttendeeItem(new Attendee(Integer.parseInt(recent))));
            }
            catch (NumberFormatException e) 
            {
                // TODO: Remove item from array
            }

            notifyDataSetChanged();
        }

        public long getItemId(int position)
        {
            return (long) position;
        }

        public Item getItem(int position)
        {
            return items.get(position);
        }

        public boolean isEnabled(int position)
        {
            return !(getItem(position) instanceof HeaderItem);
        }

        public int getCount()
        {
            return items.size();
        }

        public int getViewTypeCount()
        {
            return TYPE_COUNT;
        }

        public int getItemViewType(int position)
        {
            return getItem(position).getViewType();
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            Item item = getItem(position);
            convertView = item.getView(inflater, convertView);
            convertView.setTag(item);

            return convertView;
        }

        public static abstract class Item
        {
            public abstract int getViewType();
            public abstract void onClick(Context context);
            public abstract View getView(LayoutInflater inflater, View convertView);
        }

        public static class HeaderItem extends Item
        {
            private String label;

            public HeaderItem(String label)
            {
                this.label = label;
            }

            public int getViewType()
            {
                return TYPE_HEADER_ITEM;
            }

            public void onClick(Context context)
            {
                return;
            }

            public View getView(LayoutInflater inflater, View convertView)
            {
                if (convertView == null)
                    convertView = inflater.inflate(R.layout.drawer_header, null);

                ((TextView) convertView.findViewById(R.id.drawer_header_label)).setText(label);

                return convertView;
            }
        }

        public static abstract class ListItem extends Item implements ListView.OnItemClickListener
        {
            private String label;
            private int icon;

            public ListItem(int icon, String label)
            {
                this.icon = icon;
                this.label = label;
            }

            public int getViewType()
            {
                return TYPE_LIST_ITEM;
            }

            public View getView(LayoutInflater inflater, View convertView)
            {
                if (convertView == null)
                    convertView = inflater.inflate(R.layout.drawer_item, null);

                ((TextView) convertView.findViewById(R.id.drawer_item_label)).setText(label);
                ((ImageView) convertView.findViewById(R.id.drawer_item_icon)).setImageResource(icon);

                return convertView;
            }

            public void onItemClick(AdapterView parent, View view, int position, long id)
            {
                onClick(parent.getContext());
            }
        }

        public static class AttendeeItem extends ListItem
        {
            private Attendee attendee;

            public AttendeeItem(Attendee attendee)
            {
                super(getIconResource(attendee), attendee.getName());

                this.attendee = attendee;
            }

            public void onClick(Context context)
            {
                Intent intent = new Intent(context, ScheduleActivity.class);
                intent.putExtra("attendeeId", attendee.getId());
                context.startActivity(intent);
            }

            private static int getIconResource(Attendee attendee)
            {
                switch (attendee.getType())
                {
                    case CLASS:    return R.drawable.ic_school_black_24dp;
                    case STAFF:    return R.drawable.ic_person_black_24dp;
                    case FACILITY: return R.drawable.ic_home_black_24dp;
                    default:       return R.drawable.ic_help_black_24dp;
                }
            }
        }

        public static class IntentItem<T> extends ListItem
        {
            private Class<?> klass;

            public IntentItem(int icon, String label, Class<?> klass)
            {
                super(icon, label);

                this.klass = klass;
            }

            public void onClick(Context context)
            {
                Intent intent = new Intent(context, klass);
                context.startActivity(intent);
            }
        }
    }
}
