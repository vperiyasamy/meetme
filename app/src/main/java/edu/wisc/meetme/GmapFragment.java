package edu.wisc.meetme;

import android.*;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yaphet on 12/10/16.
 */

public class GmapFragment extends Fragment implements OnMapReadyCallback {
    // Initialize the global variables.
    private GoogleMap mMap;
    private View view;
    LocationManager locationManager;
    LocationListener locationListener;
    String latestLat;
    String latestLon;
    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);


    // This function is reached if the phone doesn't have proper permission such as certain level
    // of SDK or ability to detect location.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

                    //Toast.makeText(getActivity(), "Toast", Toast.LENGTH_SHORT);
                }
            }
        }
    }


    // This function assigns the map fragment to the current fragment in the navigation drawer.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gmap, container, false);
        return view;
    }


    // This function initializes a map fragment for the current fragment.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.gmap);
        fragment.getMapAsync(this);
    }


    // This function is used to show the content of the map, such as marker of geo-location.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Create a location manager to organize the map.
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Create a location listener to respond to the user input.
        locationListener = new LocationListener() {

            // Update the user's location once the user moves.
            @Override
            public void onLocationChanged(Location location) {
                // Add a marker in current location and move the camera.
                // Receive the current user location and show it on the map.
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));


                // Store the current user location to the local memory.

                latestLat = Double.toString(location.getLatitude());
                latestLon = Double.toString(location.getLongitude());

                sharedPreferences.edit().putString("Latitude", latestLat).apply();
                sharedPreferences.edit().putString("Longitude", latestLon).apply();


                // To get the location from local memory
                //String userName = sharedPreferences.getString("Latitude", "");
                //String userName = sharedPreferences.getString("Longitude", "");

                // Create a geocoder to retrieve the user address based on its geo-location.
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    // Create a list to store the address such as the street number, city, or state.
                    List<android.location.Address> listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if (listAddress != null && listAddress.size() > 0) {
                        Log.i("PlaceInfo", listAddress.get(0).toString());

                        String address = "";

                        if (listAddress.get(0).getSubThoroughfare() != null) {
                            address += listAddress.get(0).getSubThoroughfare() + " ";
                        }

                        if (listAddress.get(0).getThoroughfare() != null) {
                            address += listAddress.get(0).getThoroughfare() + ", ";
                        }

                        if (listAddress.get(0).getLocality() != null) {
                            address += listAddress.get(0).getLocality() + ", ";
                        }

                        if (listAddress.get(0).getPostalCode() != null) {
                            address += listAddress.get(0).getPostalCode() + ", ";
                        }

                        if (listAddress.get(0).getCountryName() != null) {
                            address += listAddress.get(0).getCountryName();
                        }

                        Toast.makeText(getActivity(), address, Toast.LENGTH_LONG).show();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        // If device is running SDK < 23
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                // ask for permission
                //Toast.makeText(getActivity(), "toast 1", Toast.LENGTH_SHORT);
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                // Here for some reason the last known location keeps returning a null pointer, thus we
                // assign a default location to show on the map. When the user moves, the current location
                // will update.
                if (lastKnownLocation == null) {

                    LatLng userLocation = new LatLng(43.071941, -89.410203);
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    // Store the current user location to the local memory.

                    latestLat = Double.toString(43.071941);
                    latestLon = Double.toString(-89.410203);

                    sharedPreferences.edit().putString("Latitude", latestLat).apply();
                    sharedPreferences.edit().putString("Longitude", latestLon).apply();


                } else {

                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    // Store the current user location to the local memory.

                    latestLat = Double.toString(lastKnownLocation.getLatitude());
                    latestLon = Double.toString(lastKnownLocation.getLongitude());

                    sharedPreferences.edit().putString("Latitude", latestLat).apply();
                    sharedPreferences.edit().putString("Longitude", latestLon).apply();
                }
            }
        }

    }


}
