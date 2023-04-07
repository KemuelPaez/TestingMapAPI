package com.example.mapapi;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.model.mutation.ArrayTransformOperation;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap googleMap;
    public SearchView searchView;
    public String[] places = {"University of St. Lasalle", "Bacolod Silay Airport", "888 China Town", "SM Mall", "Bacolod Lagoon"};

    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterItems;

    LocationRequest locRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    boolean isPermissionGranter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);

        adapterItems = new ArrayAdapter<String>(this,R.layout.list_places,places);

        autoCompleteTextView.setAdapter(adapterItems);

        checkPermission();

        if (checkGooglePlayServices()) {
            SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.container, supportMapFragment).commit();
            supportMapFragment.getMapAsync(this);
            if (isPermissionGranter) {
                CheckGps();
            }

        } else {
            Toast.makeText(this, "GooglePlay Services Unavailable", Toast.LENGTH_SHORT).show();

        }

        // TODO Search view functionality try (put the method on or after OnMapReady instead of before onMapReady)

    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {

            return true;

        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(MainActivity.this, "Canceled Dialog", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }

        return false;
    }

    private void checkPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissionGranter = true;
                Toast.makeText(MainActivity.this, "Location Permission Granted!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Set UI and Controls on map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().isCompassEnabled();
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setTrafficEnabled(true);

        // Enabling the Current location/pos of user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String places = parent.getItemAtPosition(position).toString();
                MarkerOptions markerOptions = new MarkerOptions();

                if(parent.getItemAtPosition(position).equals("University of St. Lasalle")){
                    Toast.makeText(MainActivity.this, "Marker is set in "+places, Toast.LENGTH_SHORT).show();

                    // Coords of lasalle
                    LatLng latlng = new LatLng(10.678417, 122.962483955146);

                    // Set markers
                    markerOptions.title("Marked in " + places);
                    markerOptions.position(latlng);
                    googleMap.addMarker(markerOptions);

                    // Set & Animate Camera
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
                    googleMap.animateCamera(cameraUpdate);

                } else if (parent.getItemAtPosition(position).equals("Bacolod Silay Airport")){
                    Toast.makeText(MainActivity.this, "Marker is set in "+places, Toast.LENGTH_SHORT).show();

                    // Coords of airport
                    LatLng latlng = new LatLng(10.777820237859661, 123.01351904869081);

                    // Set markers
                    markerOptions.title("Marked in " + places);
                    markerOptions.position(latlng);
                    googleMap.addMarker(markerOptions);

                    // Set & Animate Camera
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
                    googleMap.animateCamera(cameraUpdate);

                } else if (parent.getItemAtPosition(position).equals("888 China Town")){
                    Toast.makeText(MainActivity.this, "Marker is set in "+places, Toast.LENGTH_SHORT).show();

                    // Coords of 888
                    LatLng latlng = new LatLng(10.673676537839905, 122.94941425323488);

                    // Set markers
                    markerOptions.title("Marked in " + places);
                    markerOptions.position(latlng);
                    googleMap.addMarker(markerOptions);

                    // Set & Animate Camera
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
                    googleMap.animateCamera(cameraUpdate);

                } else if (parent.getItemAtPosition(position).equals("SM Mall")){
                    Toast.makeText(MainActivity.this, "Marker is set in "+places, Toast.LENGTH_SHORT).show();

                    // Coords of SM
                    LatLng latlng = new LatLng(10.671088169829, 122.94386744499208);

                    // Set markers
                    markerOptions.title("Marked in " + places);
                    markerOptions.position(latlng);
                    googleMap.addMarker(markerOptions);

                    // Set & Animate Camera
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
                    googleMap.animateCamera(cameraUpdate);

                } else if (parent.getItemAtPosition(position).equals("Bacolod Lagoon")){
                    Toast.makeText(MainActivity.this, "Marker is set in "+places, Toast.LENGTH_SHORT).show();

                    // Coords of lagoon
                    LatLng latlng = new LatLng(10.675716641291453, 122.95286893844606);

                    // Set markers
                    markerOptions.title("Marked in " + places);
                    markerOptions.position(latlng);
                    googleMap.addMarker(markerOptions);

                    // Set & Animate Camera
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
                    googleMap.animateCamera(cameraUpdate);
                }

            }
        });

    }

    private void CheckGps() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    // If GPS is already enabled
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    GetCurrentUpdate();
                    Toast.makeText(MainActivity.this, "GPS currently enabled", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(MainActivity.this, 101);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    }
                    // If settings is unavailable
                    if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        Toast.makeText(MainActivity.this, "Settings Unavailable", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void GetCurrentUpdate() {
        locRequest = LocationRequest.create();
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locRequest.setInterval(5000);
        locRequest.setFastestInterval(3000);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }
        fusedLocationProviderClient.requestLocationUpdates(locRequest, new LocationCallback() {
            // This method will display the Lat and Long of the current location via toast of the user and will
            // update everytime via loop to where the current loc of the user.
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Comment out toast to stop the toast update.
                // Toast.makeText(MainActivity.this, "Location: "+locationResult.getLastLocation().getLatitude()+": "+locationResult.getLastLocation().getLongitude(), Toast.LENGTH_SHORT).show();
            }
        }, Looper.getMainLooper());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            // GPS is enabled
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "GPS is now enabled!", Toast.LENGTH_SHORT).show();
            }
            // If GPS is Canceled
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Canceled Connection to GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}