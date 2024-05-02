package org.me.gcu.abdulrahman_sumaya_s2110916;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {
    private GoogleMap googleMap;

    private final LatLng[] locationCoordinates = {
            new LatLng(55.8642, -4.2518), // Glasgow
            new LatLng(51.5074, -0.1278), // London
            new LatLng(40.7128, -74.0060), // New York
            new LatLng(21.5135, 55.9233), // Oman
            new LatLng(-20.3484, 57.5522), // Mauritius
            new LatLng(23.8103, 90.4125) // Bangladesh
    };

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap map) {
            googleMap = map;

            String locationName = getArguments().getString("locationName");

            int locationIndex = -1;
            for (int i = 0; i < ForecastActivity.locations.length; i++) {
                if (ForecastActivity.locations[i][0].equals(locationName)) {
                    locationIndex = i;
                    break;
                }
            }

            if (locationIndex != -1) {
                LatLng locationCoordinates = MapsFragment.this.locationCoordinates[locationIndex];
                googleMap.addMarker(new MarkerOptions().position(locationCoordinates).title(locationName));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationCoordinates, 8));
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}