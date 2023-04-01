package com.example.mapapi;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap googleMap;
    public ImageView imageSearchBtn;
    public EditText inputLocation;

    boolean isPermissionGranter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSearchBtn = findViewById(R.id.imageSearchBtn);
        inputLocation = findViewById(R.id.inputLocation);

        checkPermission();

        if (isPermissionGranter) {
            if (checkGooglePlayServices()) {
                SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
                getSupportFragmentManager().beginTransaction().add(R.id.container, supportMapFragment).commit();
                supportMapFragment.getMapAsync(this);

            } else {
                Toast.makeText(this, "GooglePlay Services Unavailable", Toast.LENGTH_SHORT).show();
            }
        }


        // TODO: 01/04/2023 Geocoding search function still not working. Need to find more tutorials.

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
                Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();

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

        LatLng latlng = new LatLng(10.675716641291453,122.95286893844606);

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

        // Enabling the Current location/pos of user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);


    }


}