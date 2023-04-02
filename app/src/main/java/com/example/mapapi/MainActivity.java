package com.example.mapapi;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.CurrentLocationRequest;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap googleMap;
    public ImageView imageSearchBtn;
    public ImageButton soyBtn;
    public EditText inputLocation;

    LocationRequest locRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    boolean isPermissionGranter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSearchBtn = findViewById(R.id.imageSearchBtn);
        soyBtn = findViewById(R.id.soy);
        inputLocation = findViewById(R.id.inputLocation);

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


        // TODO: 01/04/2023 Geocoding search function still not working. Need to find more tutorials iban outdated :).

//        imageSearchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String input = inputLocation.getText().toString();
//                if (input == null){
//                    Toast.makeText(MainActivity.this, "Empty Field", Toast.LENGTH_SHORT).show();
//                }else {
//
//                Geocoder geocoder = new Geocoder(MainActivity.this,Locale.getDefault());
//                    Toast.makeText(MainActivity.this, "Geo work", Toast.LENGTH_SHORT).show();
//
//                try {
//                    List<Address> listAddress = geocoder.getFromLocationName(String.valueOf(inputLocation), 1);
//
//                    if (listAddress.size() > 0){
//                        LatLng latlng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
//
//                        // Set markers
//                        MarkerOptions markerOptions = new MarkerOptions();
//                        markerOptions.title("Searched Marker");
//                        markerOptions.position(latlng);
//                        googleMap.addMarker(markerOptions);
//
//                        // Set & Animate Camera
//                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 15);
//                        googleMap.animateCamera(cameraUpdate);
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    }
//
//                }
//
//            }
//        });

        soyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,ImportantActivity.class);
                startActivity(i);
            }
        });

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

        LatLng latlng = new LatLng(10.675716641291453, 122.95286893844606);

        // Set markers
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Searched Marker");
        markerOptions.position(latlng);
        googleMap.addMarker(markerOptions);

        // Set & Animate Camera
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 15);
        googleMap.animateCamera(cameraUpdate);


        // Set UI and Controls on map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Enabling the Current location/pos of user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }
        googleMap.setMyLocationEnabled(true);

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
                Toast.makeText(MainActivity.this, "Location: "+locationResult.getLastLocation().getLatitude()+": "+locationResult.getLastLocation().getLongitude(), Toast.LENGTH_SHORT).show();
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