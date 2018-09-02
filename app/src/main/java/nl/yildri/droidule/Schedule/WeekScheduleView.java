package nl.yildri.droidule.Schedule;

import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.yildri.droidule.R;
import nl.yildri.droidule.Util.MiscUtil;

public class WeekScheduleView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener, EventReceiver
{
    private float startHour = 8.5f;
    private float endHour = 16.f;
    private int startDay = 1;
    private int endDay = 5;

    private float hourHeight = 64; // dp

    private boolean currentWeek = false;
    private int year;
    private int week;

    private final Context context;
    private final WeekScheduleView self;
    private ScrollView scrollView;

    private ArrayList<EventView> events;
    private ArrayList<ViewGroup> dayColumns;

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public WeekScheduleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        inflate(context, R.layout.weekscheduleview, this);

        this.context = context;
        this.self = this;

        events = new ArrayList<EventView>();
        dayColumns = new ArrayList<ViewGroup>();

        for (int i = 0; i < 6; i++)
        {
            dayColumns.add(
                    ((ViewGroup)
                    ((ViewGroup)
                        findViewById(R.id.weekschedule_daycolumns))
                        .getChildAt(i)));

            Log.d("Droidule", i + ": " + dayColumns.get(i));
        }

        for (int h = (int) Math.ceil(startHour); h <= endHour; h++)
        {
            View marker = inflate(context, R.layout.weekschedule_hourmarker, null);
            ((TextView) marker.findViewById(R.id.weekschedule_hourmarker_label)).setText(h + "h");
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = (int) MiscUtil.getPx(((h - startHour) * hourHeight), context.getResources());
            marker.setLayoutParams(params);
            dayColumns.get(0).addView(marker);
        }

        scrollView = (ScrollView) findViewById(R.id.scrollview);
    }

    public void addEvent(final Event event)
    {
        EventView eventView = new EventView(context, null, event);

        events.add(eventView);
        dayColumns.get(event.getDay()).addView(eventView);

        float height = MiscUtil.getPx((event.getEnd().toFloat() - event.getStart().toFloat()) * hourHeight + 1, context.getResources());
        float y = MiscUtil.getPx((event.getStart().toFloat() - startHour) * hourHeight, context.getResources());

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) height);
        params.topMargin = (int) y;

        ((TextView) eventView.findViewById(R.id.weekschedule_event_primary_text)).setText(event.getAbbreviation());

        String secondaryText = "";
        for (int i = 0; i < event.getFacilities().size(); i++)
        {
            if (i >= 5)
            {
                secondaryText += "⋮"; // U+22EE Vertical Ellipsis
                break;
            }

            secondaryText += event.getFacilities().get(i).getName() + "\n";
        }

        ((TextView) eventView.findViewById(R.id.weekschedule_event_secondary_text)).setText(secondaryText);

        eventView.findViewById(R.id.weekschedule_event_color).setBackgroundColor(event.getColor());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            eventView.setElevation(MiscUtil.getPx(4, context.getResources()));

        eventView.setLayoutParams(params);
        eventView.setOnClickListener(self);
        eventView.setOnLongClickListener(self);
    }

    public void clear()
    {
        events.clear();

        for (int i = 1; i <= 5; i++)
        {
            ViewGroup column = ((ViewGroup) ((ViewGroup)
                        findViewById(R.id.weekschedule_daycolumns))
                        .getChildAt(i));

            column.removeViews(1, column.getChildCount() - 1);
        }
    }

    public boolean canScrollUp()
    {
        return scrollView.canScrollVertically(-1);
    }

    public void setEvents(ArrayList<Event> input)
    {
        clear();

        for (Event event : input)
        {
            addEvent(event);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener)
    {
        this.onLongClickListener = onLongClickListener;
    }

    public void onClick(View view)
    {
        if (onClickListener != null) onClickListener.onClick(view);
    }

    public boolean onLongClick(View view)
    {
        if (onLongClickListener != null) return onLongClickListener.onLongClick(view);

        return false;
    }

    public void setWeek(int year, int week)
    {
        this.year = year;
        this.week = week;
    }

    public static class EventView extends RelativeLayout
    {
        private Event event;

        public EventView(Context context, AttributeSet attrs, Event event)
        {
            super(context, attrs);

            inflate(context, R.layout.weekschedule_event, this);

            this.event = event;

            this.setBackgroundColor(0xffffffff);
        }

        public Event getEvent()
        {
            return event;
        }
    }
}
