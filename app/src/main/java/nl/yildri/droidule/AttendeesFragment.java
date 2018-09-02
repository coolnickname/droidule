package nl.yildri.droidule;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ClassCastException;
import java.util.List;

import nl.yildri.droidule.R;
import nl.yildri.droidule.Xedule.Attendee;
import nl.yildri.droidule.Xedule.Location;
import nl.yildri.droidule.Xedule.Xedule;

public class AttendeesFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshLayout.OnChildScrollUpCallback {
    private OnAttendeeSelectedListener listener;
    private AttendeesAdapter adapter;
    private Location location;
    private SwipeRefreshLayout swipeLayout;

    public interface OnAttendeeSelectedListener
    {
        public void onAttendeeSelected(Attendee attendee);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.attendees_fragment, container, false);

        swipeLayout = (SwipeRefreshLayout) ((ViewGroup) view).findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setOnChildScrollUpCallback(this);
        //swipeLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        EditText searchInput = (EditText) view.findViewById(R.id.attendees_search);

        searchInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3)
            {
                if (adapter == null) return;
                adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void afterTextChanged(Editable arg0)
            {

            }
        });

        return view;
    }

    public void updateLocation(Location location)
    {
        this.location = location;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        location = new Location(getArguments().getInt("locationId"));

        try
        {
            listener = (OnAttendeeSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnAttendeeSelectedListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ArrayList<Attendee> attendees = location.getAttendees();
        if (attendees.isEmpty())
        {
            refresh();
        }
        else
        {
            populateList(attendees);
        }
    }

    @Override
    public void onRefresh()
    {
        refresh();
    }

    @Override
    public boolean canChildScrollUp(SwipeRefreshLayout parent, View child){
        //We need to check if the ListView can scroll, as by default it checks the first child
        // which is the LinearLayout.
        LinearLayout layout = (LinearLayout) child;
        ListView list = (ListView) layout.getChildAt(2);
        return list.canScrollVertically(-1);
    }

    public void refresh()
    {
        new AsyncTask<Void, Void, ArrayList<Attendee>>()
        {
            protected ArrayList<Attendee> doInBackground(Void... _)
            {
                Xedule.updateAttendees(location, null);
                return location.getAttendees();
            }

            protected void onPostExecute(ArrayList<Attendee> attendees)
            {
                populateList(attendees);
                swipeLayout.setRefreshing(false);
            }
        }.execute();
    }

    public void populateList(ArrayList<Attendee> attendees)
    {
        if (getActivity() == null) return; // The activity could have been destroyed since we're coming
                                           //  from a background job

        ((ViewGroup) getView()).findViewById(R.id.list_loading).setVisibility(View.GONE);

        if (attendees.isEmpty())
        {
            ((ViewGroup) getView()).findViewById(R.id.list_empty).setVisibility(View.VISIBLE);
        }
        else
        {
            adapter = new AttendeesAdapter(getActivity(), attendees);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        listener.onAttendeeSelected(adapter.getItem(position));
    }

    private class AttendeesAdapter extends BaseAdapter implements Filterable
    {
        private Activity activity;
        private ArrayList<Attendee> data;
        private ArrayList<Attendee> originalData;
        private AttendeeFilter filter;
        private LayoutInflater inflater;

        public AttendeesAdapter(Activity a, ArrayList<Attendee> input)
        {
            if (a == null) return; // The activity could have been destroyed since we're coming
                                   //  from a background job

            activity = a;
            data = input;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void clear()
        {
            data.clear();

            this.notifyDataSetChanged();
        }

        public int getCount()
        {
            return data.size();
        }

        public Attendee getItem(int position)
        {
            return data.get(position);
        }

        public long getItemId(int position)
        {
            return position;
        }

        public void sort()
        {
            Collections.sort(data);
            this.notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            if(convertView == null)
                view = inflater.inflate(R.layout.attendee_item, null);


            TextView name = (TextView) view.findViewById(R.id.attendee_name);
            TextView type = (TextView) view.findViewById(R.id.attendee_type);

            Attendee att = data.get(position);

            name.setText(att.getName());
            type.setText(att.getType().label);

            return view;
        }

        @Override
        public Filter getFilter()
        {
            if (filter == null)
                filter = new AttendeeFilter();

            return filter;
        }

        private class AttendeeFilter extends Filter
        {
            @Override
            protected FilterResults performFiltering(CharSequence query)
            {
                FilterResults results = new FilterResults();

                if (originalData == null)
                    originalData = new ArrayList<Attendee>(data);

                if (query == null || query.length() == 0)
                {
                    results.values = originalData;
                    results.count = originalData.size();
                }
                else
                {
                    String queryString = query.toString().toLowerCase();

                    ArrayList<Attendee> values = new ArrayList<Attendee>(originalData);

                    int count = values.size();
                    ArrayList<Attendee> newValues = new ArrayList<Attendee>();

                    for (int i = 0; i < count; i++)
                    {
                        Attendee value = values.get(i);
                        String valueName = value.getName().toLowerCase();

                        if (valueName.indexOf(queryString) >= 0)
                            newValues.add(value);
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                data = (ArrayList<Attendee>) results.values;

                if (results.count > 0) notifyDataSetChanged();
                else notifyDataSetInvalidated();
            }
        }
    }
}
