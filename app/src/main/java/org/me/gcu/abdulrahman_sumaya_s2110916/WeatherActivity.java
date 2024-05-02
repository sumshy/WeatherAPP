package org.me.gcu.abdulrahman_sumaya_s2110916;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WeatherActivity extends AppCompatActivity {

    private ViewGroup rootView;
    private TextView locationTextView, dateTextView, minTempTextView,
            maxTempTextView, humidityTextView, sunriseTextView, sunsetTextView,
            pollutionTextView, windTextView, visibilityTextView;
    private final String[][] locations = {
            {"Glasgow", "2648579"},
            {"London", "2643743"},
            {"New York", "5128581"},
            {"Oman", "287286"},
            {"Mauritius", "934154"},
            {"Bangladesh", "1185241"}
    };
    private int currentLocationIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNetworkAvailable()) {
            setContentView(R.layout.activity_main);

            rootView = findViewById(android.R.id.content);
            locationTextView = findViewById(R.id.location);
            dateTextView = findViewById(R.id.date);
            minTempTextView = findViewById(R.id.minimumtemp);
            maxTempTextView = findViewById(R.id.maximumtemp);
            humidityTextView = findViewById(R.id.humidityvalue);
            sunriseTextView = findViewById(R.id.sunriseValue);
            sunsetTextView = findViewById(R.id.sunsetValue);
            pollutionTextView = findViewById(R.id.PollutionValue);
            windTextView = findViewById(R.id.WindValue);
            visibilityTextView = findViewById(R.id.visibilityValue);

            Spinner locationSpinner = findViewById(R.id.locationSpinner);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (String[] location : locations) {
                spinnerAdapter.add(location[0]);
            }
            locationSpinner.setAdapter(spinnerAdapter);

            locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentLocationIndex = position;
                    updateLocation();
                    startFetchingWeatherData();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            Button viewMoreButton = findViewById(R.id.viewMore);

            viewMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Inflate the forecast layout
                    View forecastView = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_activity, rootView, false);

                    // Replace the content of rootView with forecastView
                    rootView.removeAllViews(); // Clear existing views from rootView
                    rootView.addView(forecastView); // Add forecastView to rootView

                    // Get the selected location index
                    int selectedLocationIndex = currentLocationIndex;

                    // Pass the selected location index to the forecast activity
                    Intent intent = new Intent(WeatherActivity.this, ForecastActivity.class);
                    intent.putExtra("selectedLocationIndex", selectedLocationIndex);
                    startActivity(intent);
                }
            });

            startFetchingWeatherData();
        } else {
            setContentView(R.layout.activity_no_internet);

            Button okButton = findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Close the app
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void startFetchingWeatherData() {
        new Thread(() -> {
            try {
                String locationID = locations[currentLocationIndex][1];
                String urlSource = "https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/" + locationID;
                URL url = new URL(urlSource);
                URLConnection connection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    result.append(inputLine);
                }
                reader.close();

                parseWeatherData(result.toString());
            } catch (IOException e) {
                Log.e("WeatherFetch", "Error fetching weather data: " + e.getMessage());
            }
        }).start();
    }

    private void parseWeatherData(String weatherData) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new java.io.StringReader(weatherData));
            int eventType = parser.getEventType();
            String date = "", minTemp = "", maxTemp = "", humidity = "",
                    sunrise = "", sunset = "", pollution = "", wind = "", visibility = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("item".equals(tagName)) {
                        date = minTemp = maxTemp = humidity = sunrise = sunset = pollution = wind = visibility = "";
                    } else if ("description".equals(tagName)) {
                        String description = parser.nextText();
                        String[] parts = description.split(", ");
                        for (String part : parts) {
                            String[] keyValue = part.split(":");
                            if (keyValue.length == 2) {
                                String key = keyValue[0].trim().toLowerCase();
                                String value = keyValue[1].trim();
                                switch (key) {
                                    case "maximum temperature":
                                        maxTemp = value;
                                        break;
                                    case "minimum temperature":
                                        minTemp = value;
                                        break;
                                    case "wind speed":
                                        wind = value;
                                        break;
                                    case "humidity":
                                        humidity = value;
                                        break;
                                    case "pollution":
                                        pollution = value;
                                        break;
                                    case "visibility":
                                        visibility = value;
                                        break;
                                }
                            } else if (part.contains("Sunrise:")) {
                                sunrise = part.substring(part.indexOf("Sunrise:") + "Sunrise:".length()).trim();
                            } else if (part.contains("Sunset:")) {
                                sunset = part.substring(part.indexOf("Sunset:") + "Sunset:".length()).trim();
                            }
                        }
                    } else if ("pubDate".equals(tagName)) {
                        String pubDate = parser.nextText();
                        String[] parts = pubDate.split(", ");
                        if (parts.length >= 2) {
                            String[] dateParts = parts[1].split(" ");
                            if (dateParts.length >= 4) {
                                date = parts[0] + ", " + dateParts[0] + " " + dateParts[1] + " " + dateParts[2];
                            } else {
                                Log.e("WeatherParser", "Invalid date format: " + parts[1]);
                            }
                        } else {
                            Log.e("WeatherParser", "Invalid pubDate format: " + pubDate);
                        }
                    }
                }
                eventType = parser.next();
            }

            String finalDate = date;
            String finalMinTemp = minTemp;
            String finalMaxTemp = maxTemp;
            String finalHumidity = humidity;
            String finalSunrise = sunrise;
            String finalSunset = sunset;
            String finalPollution = pollution;
            String finalWind = wind;
            String finalVisibility = visibility;
            runOnUiThread(() -> {
                dateTextView.setText(finalDate);
                minTempTextView.setText("Min: " + finalMinTemp);
                maxTempTextView.setText("Max: " + finalMaxTemp);
                humidityTextView.setText(finalHumidity);
                sunriseTextView.setText(finalSunrise);
                sunsetTextView.setText(finalSunset);
                pollutionTextView.setText(finalPollution);
                windTextView.setText(finalWind);
                visibilityTextView.setText(finalVisibility);
            });
        } catch (XmlPullParserException | IOException e) {
            Log.e("WeatherParser", "Error parsing weather data: " + e.getMessage());
        }
    }

    private void updateLocation() {
        locationTextView.setText(locations[currentLocationIndex][0]);
    }
}