package com.example.mapapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    public Button panSelfBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        Button panSelfBtn = (Button) findViewById(R.id.panSelfCam);

        panSelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LatLng bacolod = new LatLng(10.675715, 122.952885);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(bacolod));

            }
        });



    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // init zoom min/max preference
        googleMap.setMinZoomPreference(14.0f);
        googleMap.setMaxZoomPreference(31.0f);

        LatLng bacolod = new LatLng(10.675715, 122.952885);
        // Set bounds on bacolod coordinates
        LatLngBounds bacolodBounds = new LatLngBounds(
                new LatLng(10.586913,122.910118), // NE Bounds
                new LatLng(10.712430,122.962391)); // SE Bounds

        mMap.addMarker(new MarkerOptions().position(bacolod).title("You are Here!"));
        mMap.setLatLngBoundsForCameraTarget(bacolodBounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bacolod));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(bacolod));

    }

}