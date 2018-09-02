package nl.yildri.droidule.Schedule;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import nl.yildri.droidule.R;
import nl.yildri.droidule.ScheduleActivity;
import nl.yildri.droidule.ScheduleSwipeRefreshLayout;
import nl.yildri.droidule.Theming.Colours;
import nl.yildri.droidule.Util.MiscUtil;
import nl.yildri.droidule.Xedule.Attendee;

public class WeekScheduleFragment extends Fragment implements EventReceiver,
                                                              SwipeRefreshLayout.OnRefreshListener
{
    private ScheduleActivity parent;
    private WeekScheduleView weekScheduleView;
    private Attendee attendee;
    private int year;
    private int week;
    private int weekday;
    private ScheduleSwipeRefreshLayout swipeLayout;

    private boolean refreshing;

    public interface OnEventSelectedListener
    {
        public void onEventSelected(Event event);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        swipeLayout = (ScheduleSwipeRefreshLayout) inflater.inflate(R.layout.weekschedule_fragment, container, false);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(Colours.primaryColor(this.getContext()));
        swipeLayout.setContainer(this);

        weekScheduleView = (WeekScheduleView) swipeLayout.findViewById(R.id.weekschedule);
        weekScheduleView.setWeek(year, week);

        weekScheduleView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (parent != null) parent.onEventSelected(((WeekScheduleView.EventView) view).getEvent());
                else Log.w("Droidule", "WeekScheduleFragment's parent is null!");
            }
        });

        return (View) swipeLayout;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            parent = (ScheduleActivity) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " can only be used by ScheduleActivity");
        }

        Timer timer = new Timer();
        InvalidateTimer task = new InvalidateTimer(this);
        timer.schedule(task, 60 * 1000, 60 * 1000);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ((ScheduleActivity) getActivity()).refresh(false);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(MiscUtil.getPx(8, getActivity().getResources()));
    }

    public void setEvents(final ArrayList<Event> events)
    {
        weekScheduleView.clear();
        weekScheduleView.setEvents(events);
    }

    public void setWeek(int year, int week)
    {
        this.year = year;
        this.week = week;

        if (weekScheduleView != null) weekScheduleView.setWeek(year, week);
    }

    public boolean canScrollUp()
    {
        return weekScheduleView.canScrollUp();
    }

    public void setRefreshing(boolean refreshing)
    {
        swipeLayout.setRefreshing(refreshing);
    }

    @Override
    public void onRefresh()
    {
        parent.refresh(true);
    }

    private class InvalidateTimer extends TimerTask
    {
        WeekScheduleFragment fragment;

        public InvalidateTimer(WeekScheduleFragment fragment)
        {
            this.fragment = fragment;
        }

        @Override
        public void run()
        {
        }
    }
}
