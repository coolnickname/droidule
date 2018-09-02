package nl.yildri.droidule.Schedule;

import java.util.ArrayList;

public interface EventReceiver {
    public void setEvents(ArrayList<Event> events);

    public void setWeek(int year, int week);
}
