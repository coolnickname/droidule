package nl.yildri.droidule;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import nl.yildri.droidule.Schedule.WeekScheduleFragment;

public class ScheduleSwipeRefreshLayout extends SwipeRefreshLayout {
    private WeekScheduleFragment parent;

    public ScheduleSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ScheduleSwipeRefreshLayout(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void setContainer(WeekScheduleFragment parent) {
        this.parent = parent;
    }

    @Override
    public boolean canChildScrollUp() {
        return parent.canScrollUp();
    }
}
