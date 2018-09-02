package nl.yildri.droidule.Xedule.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

import nl.yildri.droidule.Droidule;
import nl.yildri.droidule.Schedule.Event;
import nl.yildri.droidule.Util.Constants;
import nl.yildri.droidule.Util.DebugData;
import nl.yildri.droidule.Xedule.Attendee;
import nl.yildri.droidule.Xedule.Location;
import nl.yildri.droidule.Xedule.Organisation;

/**
 * Created by Yildri on 21/01/2018.
 */

public class XeduleAPI {
    public static ArrayList<Organisation> getOrganisations(){
        Document page = null;
        ArrayList<Organisation> organisations = new ArrayList<>();

        try {
            page = Jsoup.parse(new URL("https://roosters.xedule.nl/"), 10000);
        }catch(Exception e){
            e.printStackTrace();
            Log.w("XeduleAPI", "Data: " + "URL not working in XeduleAPI#getOrganisations");
            return null;
        }

        Elements rawOrganisations = page.body().getElementsByClass("organisatie");

        for(Element element : rawOrganisations){
            String HTML = element.html();

            int id = Integer.parseInt(HTML.substring(
                    HTML.indexOf("<a href=\"/Organisatie/OrganisatorischeEenheid/") + 46,
                    HTML.indexOf("?Code=")));

            String name = HTML.substring(
                    HTML.indexOf("\">") + 2,
                    HTML.indexOf("</a>"));

            //TODO: Allow for other schools once I fix fetching attendees.
            if(id != 13) continue;

            organisations.add(new Organisation(id, name));
        }

        page.empty();
        return organisations;
    }

    public static ArrayList<Location> getLocations(Organisation organisation){
        ArrayList<Location> locations = new ArrayList<>();

        try {
            URL url =  new URL(organisation.getURL() + Constants.LOCATIONS_ENDPOINT);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if(connection.getResponseCode() != 200){
                Log.w("XeduleAPI", "Error while getting locations: " + connection.getResponseCode());
                return locations;
            }

            //Read the response
            ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

            for(byte[] b = new byte[512]; 0 < connection.getInputStream().read(b); outputBytes.write(b));
            JSONArray locationArray = new JSONArray(new String(outputBytes.toByteArray(), StandardCharsets.UTF_8));

            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject locationJSON = locationArray.getJSONObject(i);

                locations.add(new Location(Integer.parseInt(locationJSON.getString("id")),
                        locationJSON.getString("code"), organisation));
            }

            outputBytes.close();
            connection.disconnect();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return locations;
    }

    public static ArrayList<Attendee> getAttendees(Location location) {
        ArrayList<Attendee> attendees = new ArrayList<>();

        URL url;
        HttpURLConnection connection;
        ByteArrayOutputStream outputBytes;
        JSONArray locationArray;

        try {

            //TODO: Fix remote polling of the classes, facilities and staff

            //Get classes
            url = new URL(location.getOrganisation().getURL() + Constants.CLASSES_ENDPOINT);

            connection = (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("cookie", location.getOrganisation().getUserCookie());

            if (connection.getResponseCode() != 200) {
                Log.w("XeduleAPI", "Error while getting attendees: " + connection.getResponseCode());
                return attendees;
            }

            outputBytes = new ByteArrayOutputStream();

            for (byte[] b = new byte[512]; 0 < connection.getInputStream().read(b); outputBytes.write(b)) ;

            //locationArray = new JSONArray((new String(outputBytes.toByteArray(), StandardCharsets.UTF_8)).replaceAll("\"", "\\\""));

            locationArray = new JSONArray(DebugData.getClassesRaw());

            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject attendee = locationArray.getJSONObject(i);

                attendees.add(new Attendee(Integer.parseInt(attendee.getString("id")),
                        attendee.getString("code"), location, Attendee.Type.CLASS));

            }

            //Get facilities
            url = new URL(location.getOrganisation().getURL() + Constants.FACILITIES_ENDPOINT);

            connection = (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("cookie", location.getOrganisation().getUserCookie());

            if (connection.getResponseCode() != 200) {
                Log.w("XeduleAPI", "Error while getting attendees: " + connection.getResponseCode());
                return attendees;
            }

            outputBytes = new ByteArrayOutputStream();

            for (byte[] b = new byte[512]; 0 < connection.getInputStream().read(b); outputBytes.write(b)) ;

            //locationArray = new JSONArray(new String(outputBytes.toByteArray(), StandardCharsets.UTF_8));

            locationArray = new JSONArray(DebugData.getFacilitiesRaw());

            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject attendee = locationArray.getJSONObject(i);

                attendees.add(new Attendee(Integer.parseInt(attendee.getString("id")),
                        attendee.getString("code"), location, Attendee.Type.FACILITY));

            }

            //Get staff
            url = new URL(location.getOrganisation().getURL() + Constants.STAFF_ENDPOINT);

            connection = (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("cookie", location.getOrganisation().getUserCookie());

            if (connection.getResponseCode() != 200) {
                Log.w("XeduleAPI", "Error while getting attendees: " + connection.getResponseCode());
                return attendees;
            }

            outputBytes = new ByteArrayOutputStream();

            for (byte[] b = new byte[512]; 0 < connection.getInputStream().read(b); outputBytes.write(b)) ;

            //locationArray = new JSONArray(new String(outputBytes.toByteArray(), StandardCharsets.UTF_8));

            locationArray = new JSONArray(DebugData.getStaffRaw());

            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject attendee = locationArray.getJSONObject(i);

                attendees.add(new Attendee(Integer.parseInt(attendee.getString("id")),
                        attendee.getString("code"), location, Attendee.Type.STAFF));

            }

            //Close the variables only at the end because they are being reused.
            outputBytes.close();
            connection.disconnect();

        }catch (JSONException e){
            Log.e("XeduleAPI", "Error while updating staff, facilities and classes.");
            e.printStackTrace();
        }catch (Exception e) {
            Log.e("XeduleAPI", "Error while updating staff, facilities and classes.");
            e.printStackTrace();
        }

        return attendees;
    }

    public static ArrayList<Event> getEvents(Attendee attendee, int year, int week){
        ArrayList<Event> events = new ArrayList<>();
        String query = attendee.getLocation().getId() + "_" + year + "_" + week +"_" + attendee.getId();

        try {
            URL url =  new URL(attendee.getLocation().getOrganisation().getURL() + Constants.SCHEDULE_ENDPOINT + query);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("cookie", attendee.getLocation().getOrganisation().getUserCookie());

            if(connection.getResponseCode() != 200){
                Log.w("XeduleAPI", "Error while getting events: " + connection.getResponseCode());
                return events;
            }

            //Read the response
            ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

            for(byte[] b = new byte[512]; 0 < connection.getInputStream().read(b); outputBytes.write(b));
            JSONArray rawEventArray = new JSONArray(new String(outputBytes.toByteArray(), StandardCharsets.UTF_8));

            //The output for this request is ridiculous. Literally what the fuck.
            JSONArray eventArray = rawEventArray.getJSONObject(0).getJSONArray("apps");

            for (int i = 0; i < eventArray.length(); i++) {
                JSONObject eventJSON = eventArray.getJSONObject(i);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime((new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)).parse(eventJSON.getString("iStart").split("T")[0]));
                int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                Event event = new Event(
                        year,
                        week,
                        day,
                        new Event.Time(eventJSON.getString("iStart")),
                        new Event.Time(eventJSON.getString("iEnd")),
                        eventJSON.getString("name"),
                        Integer.parseInt(eventJSON.getString("id"))
                );

                JSONArray attendees = eventJSON.getJSONArray("atts");

                for(int i2 = 0; i2 < attendees.length(); i2++){
                    Attendee att = new Attendee(attendees.getInt(i2));
                    event.addAttendee(att);
                }

                events.add(event);
            }

            outputBytes.close();
            connection.disconnect();

        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }

        return events;
    }

    public static String getUserCookie(URL url){
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if(connection.getResponseCode() != 200){
                Log.w("XeduleAPI", "Error while getting locations: " + connection.getResponseCode());
                return null;
            }

            //Read the set cookie
            String cookie = connection.getHeaderField("Set-Cookie");

            connection.disconnect();

            return cookie.split(";", 2)[0];

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
