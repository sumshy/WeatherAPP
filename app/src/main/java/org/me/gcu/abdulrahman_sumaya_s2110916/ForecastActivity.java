package org.me.gcu.abdulrahman_sumaya_s2110916;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ForecastActivity extends AppCompatActivity {

    private Spinner locationSpinner;
    private TextView dayTodayTextView, weatherConditionTodayTextView, minTempTodayTextView, maxTempTodayTextView;
    private TextView dayTomorrowTextView, weatherConditionTomorrowTextView, minTempTomorrowTextView, maxTempTomorrowTextView;
    private TextView dayAfterTomorrowTextView, weatherConditionAfterTomorrowTextView, minTempDayAfterTextView, maxTempDayAfterTextView;

    public static final String[][] locations = {
            {"Glasgow", "2648579"},
            {"London", "2643743"},
            {"New York", "5128581"},
            {"Oman", "287286"},
            {"Mauritius", "934154"},
            {"Bangladesh", "1185241"}
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forecast_activity);

        locationSpinner = findViewById(R.id.locationSpinner);

        dayTodayTextView = findViewById(R.id.DayToday);
        weatherConditionTodayTextView = findViewById(R.id.WeatherConditionToday);
        minTempTodayTextView = findViewById(R.id.mintemptoday);
        maxTempTodayTextView = findViewById(R.id.maxtemptoday);

        dayTomorrowTextView = findViewById(R.id.DayTomorrow);
        weatherConditionTomorrowTextView = findViewById(R.id.WeatherConditionTomorrow);
        minTempTomorrowTextView = findViewById(R.id.mintemptomorrow);
        maxTempTomorrowTextView = findViewById(R.id.maxtemptomorrow);

        dayAfterTomorrowTextView = findViewById(R.id.DayAfterTomorrow);
        weatherConditionAfterTomorrowTextView = findViewById(R.id.WeatherConditionAfterTomorrow);
        minTempDayAfterTextView = findViewById(R.id.mintempdayafter);
        maxTempDayAfterTextView = findViewById(R.id.maxtempdayafter);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getLocationNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String locationId = locations[position][1];
                String xmlLink = "https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/" + locationId;
                new FetchWeatherDataTask().execute(xmlLink);

                String selectedLocationName = locations[position][0];
                MapsFragment fragment = createMapsFragment(selectedLocationName);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private List<String> getLocationNames() {
        List<String> locationNames = new ArrayList<>();
        for (String[] location : locations) {
            locationNames.add(location[0]);
        }
        return locationNames;
    }

    private MapsFragment createMapsFragment(String locationName) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString("locationName", locationName);
        fragment.setArguments(args);
        return fragment;
    }

    private class FetchWeatherDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                return parseInputStream(stream);
            } catch (Exception e) {
                Log.e("ForecastActivity", "Error fetching weather data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                parseWeatherData(result);
            } else {
                Log.e("ForecastActivity", "Weather data is null");
            }
        }

        private String parseInputStream(InputStream stream) throws IOException {
            java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        private void parseWeatherData(String weatherData) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(weatherData));

                int eventType = parser.getEventType();
                int dayCount = 0;
                String day = "", weatherCondition = "", minTemp = "", maxTemp = "";

                while (eventType != XmlPullParser.END_DOCUMENT && dayCount < 3) {
                    if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                        while (!(eventType == XmlPullParser.END_TAG && "item".equals(parser.getName()))) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if ("title".equals(parser.getName())) {
                                    String title = parser.nextText();
                                    int colonIndex = title.indexOf(":");
                                    if (colonIndex != -1) {
                                        day = title.substring(0, colonIndex).trim();
                                        weatherCondition = title.substring(colonIndex + 1).trim();
                                    }
                                } else if ("description".equals(parser.getName())) {
                                    String description = parser.nextText();
                                    String[] parts = description.split(",");
                                    for (String part : parts) {
                                        if (part.contains("Maximum Temperature")) {
                                            maxTemp = part.replace("Maximum Temperature:", "").trim();
                                        } else if (part.contains("Minimum Temperature")) {
                                            minTemp = part.replace("Minimum Temperature:", "").trim();
                                        }
                                    }
                                }
                            }
                            eventType = parser.next();
                        }

                        switch (dayCount) {
                            case 0:
                                dayTodayTextView.setText(day);
                                weatherConditionTodayTextView.setText(weatherCondition);
                                minTempTodayTextView.setText(minTemp);
                                maxTempTodayTextView.setText(maxTemp);
                                break;
                            case 1:
                                dayTomorrowTextView.setText(day);
                                weatherConditionTomorrowTextView.setText(weatherCondition);
                                minTempTomorrowTextView.setText(minTemp);
                                maxTempTomorrowTextView.setText(maxTemp);
                                break;
                            case 2:
                                dayAfterTomorrowTextView.setText(day);
                                weatherConditionAfterTomorrowTextView.setText(weatherCondition);
                                minTempDayAfterTextView.setText(minTemp);
                                maxTempDayAfterTextView.setText(maxTemp);
                                break;
                        }

                        dayCount++;
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                Log.e("ForecastActivity", "Error parsing weather data", e);
            }
        }
    }
}