package nl.yildri.droidule.Schedule;

import java.util.ArrayList;

import nl.yildri.droidule.Schedule.Event;

public interface EventReceiver
{
    public void setEvents(ArrayList<Event> events);
    public void setWeek(int year, int week);
}
