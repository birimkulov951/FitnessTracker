package com.fitnesstracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    private static final String TAG = "MapActivity";

    public static final String EXTRA_DISTANCE = "com.fitnesstracker.MapActivity.EXTRA_DISTANCE";
    public static final String EXTRA_AVERAGE_SPEED = "com.fitnesstracker.MapActivity.EXTRA_AVERAGE_SPEED";
    public static final String EXTRA_RUN_TIME = "com.fitnesstracker.MapActivity.EXTRA_RUN_TIME";
    public static final String EXTRA_DATE = "com.fitnesstracker.MapActivity.EXTRA_DATE";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 14f;

    // vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Button mStartToRunBtn, mFinishToRunBtn;
    private double currentLatitude;
    private double currentLongitude;
    private Polyline polyline;

    // helpers
    private double distance;
    private long time;
    private boolean isFinishPressed;
    private double averageSpeed;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermission();

        mStartToRunBtn = findViewById(R.id.start_to_run);
        mFinishToRunBtn = findViewById(R.id.finish_to_run);


        mFinishToRunBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                long runTime = (System.currentTimeMillis()-time)/1000;
                averageSpeed = distance/runTime;

                // if (runTime <= 180000 || distance <= 1000) {
                if (runTime <= 1 || distance <= 1) {

                    AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                    alertDialog.setTitle("You didn't run much!");
                    alertDialog.setMessage("Do you want to stop running?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finishButtonPressed();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                        }
                    });
                    alertDialog.show();

                } else {

                    finishButtonPressed();

                    String distanceData = String.valueOf(distance);
                    String runTimeData = String.valueOf((System.currentTimeMillis()-time)/1000); // todo add run time minutes and cut decimals
                    String averageSpeedData = String.valueOf(averageSpeed);
                    String dateData = String.valueOf(System.currentTimeMillis());
                    // Saving new the run data
                    Intent intent = new Intent(MapActivity.this, HistoryActivity.class);
                    intent.putExtra(EXTRA_DISTANCE, distanceData);
                    intent.putExtra(EXTRA_AVERAGE_SPEED, averageSpeedData);
                    intent.putExtra(EXTRA_RUN_TIME, runTimeData);
                    intent.putExtra(EXTRA_DATE, dateData);
                    startActivity(intent);
                }

            }
        });

        mStartToRunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartToRunBtn.setVisibility(View.INVISIBLE);
                mFinishToRunBtn.setVisibility(View.VISIBLE);
                isFinishPressed = false;
                time = System.currentTimeMillis();

                getDeviceLocation(true);

                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    PolylineOptions options = new PolylineOptions()
                            .clickable(true)
                            .width(7)
                            .color(Color.BLUE);
                    double oldLatitude = currentLatitude;
                    double oldLongitude = currentLongitude;

                    @Override
                    public void onMyLocationChange(Location location) {

                        if (isFinishPressed) {
                            distance = 0;
                            polyline.remove();

                        } else if (oldLatitude != location.getLatitude() && oldLongitude != location.getLongitude()) {

                            //getDeviceLocation(false);
                            Log.d(TAG, "onClick: current location lat lnt: " +  location.getLatitude() + ", " + location.getLongitude());
                            options.add(new LatLng(location.getLatitude(), location.getLongitude()));
                            polyline = mMap.addPolyline(options);
                            polyline.setClickable(true);
                            polyline.setWidth(5);

                            Bitmap customMarker = BitmapFactory.decodeResource(getResources(),R.drawable.marker);
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(currentLatitude, currentLongitude))
                                    .title("Start point")
                                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker)));

                            Location locationA = new Location("point A");
                            locationA.setLatitude(oldLatitude);
                            locationA.setLongitude(oldLongitude);

                            Location locationB = new Location("point B");
                            locationB.setLatitude(location.getLatitude());
                            locationB.setLongitude(location.getLongitude());

                            distance = distance + locationA.distanceTo(locationB);
                        }

                        oldLatitude = location.getLatitude();
                        oldLongitude = location.getLongitude();

                        Log.d(TAG, "onMyLocationChange: distance: " + distance);
                    }
                });

            }
        });

    }

    private void finishButtonPressed() {

        mStartToRunBtn.setVisibility(View.VISIBLE);
        mFinishToRunBtn.setVisibility(View.INVISIBLE);
        isFinishPressed = true;

        String runTimePrint = "Run time: " + (System.currentTimeMillis()-time)/1000 + " s"; // todo add run time minutes and cut decimals
        String totalDistancePrint = "Total distance: " + distance + " meters";
        String averageSpeedPrint = "Average speed: " + averageSpeed*3600 + " m/h (" + averageSpeed + " m/s)";

        Log.d(TAG, "onClick: run distance: " + totalDistancePrint);
        Log.d(TAG, "onClick: run time: " +  runTimePrint);
        Log.d(TAG, "onClick: average run speed: " + averageSpeedPrint);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");

        mMap = googleMap;
        mMap.setOnPolylineClickListener(MapActivity.this);
        mMap.setOnPolygonClickListener(MapActivity.this);

        if (mLocationPermissionsGranted) {

            getDeviceLocation(true);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }

    }

    private void getDeviceLocation(final boolean isMoveCameraRequired) {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {

                final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                Log.d(TAG, "getDeviceLocation: location" + location);
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        try {

                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: found location!");
                                Location currentLocation = (Location) task.getResult();
                                currentLatitude = currentLocation.getLatitude();
                                currentLongitude = currentLocation.getLongitude();
                                if (isMoveCameraRequired) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude),DEFAULT_ZOOM));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                                } else {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude, currentLongitude)));
                                }

                            } else {
                                Log.d(TAG, "onComplete: location not found");
                                Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Please turn on your GPS", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void initMap() {
            Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        Log.d(TAG, "onPolylineClick: getCameraPosition: " + mMap.getCameraPosition());
        Toast.makeText(this, "Route type " + polyline.getColor(), Toast.LENGTH_SHORT).show();

    }
}

