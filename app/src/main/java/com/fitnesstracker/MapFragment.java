package com.fitnesstracker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import static android.content.Context.MODE_WORLD_READABLE;

public class MapFragment extends Fragment implements GoogleMap.OnPolylineClickListener {

    private static final String TAG = "MapFragment";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 12f;

    // vars
    private Boolean mLocationPermissionsGranted = false;
    MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Button mStartToRunBtn, mFinishToRunBtn;
    private ImageButton mImageButton;
    private TextView mNoGpsSignal;
    private double currentLatitude;
    private double currentLongitude;
    private Polyline polyline;

    // helpers
    private double distance;
    private long time;
    private boolean isFinishPressed;
    private double averageSpeed;
    private Bitmap bitmap;
    private BottomNavigationView mBottomNav;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartToRunBtn = view.findViewById(R.id.start_to_run);
        mFinishToRunBtn = view.findViewById(R.id.finish_to_run);
        mNoGpsSignal = view.findViewById(R.id.no_gps_signal);
        mImageButton = view.findViewById(R.id.my_location_button);
        //mBottomNav = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_view);

        getLocationPermission();

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation(true);
            }
        });

        mFinishToRunBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                long runTime = (System.currentTimeMillis()-time)/1000;
                averageSpeed = distance/runTime;

                // if (runTime <= 180000 || distance <= 1000) {
                if (runTime <= 1 || distance <= 1) {

                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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

                    PolylineOptions options = new PolylineOptions().clickable(true).width(7).color(Color.BLUE);
                    double oldLatitude = currentLatitude;
                    double oldLongitude = currentLongitude;
                    boolean hasMarker = false;

                    @Override
                    public void onMyLocationChange(Location location) {

                        if (isFinishPressed) {
                            distance = 0;
                            polyline.remove();
                            hasMarker = false;
                        } else if (oldLatitude != location.getLatitude() && oldLongitude != location.getLongitude()) {

                            Log.d(TAG, "onClick: current location lat lnt: " +  location.getLatitude() + ", " + location.getLongitude());
                            // adding polyline
                            options.add(new LatLng(location.getLatitude(), location.getLongitude()));
                            polyline = mMap.addPolyline(options);
                            polyline.setClickable(true);
                            polyline.setWidth(7);

                            double markerLatitude = location.getLatitude();
                            double markerLongitude = location.getLongitude();
                            if (!hasMarker) {
                                hasMarker = true;
                                Bitmap customMarker = getBitmap(R.drawable.marker_24);
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(markerLatitude, markerLongitude))
                                        .title("Start point")
                                        .icon(BitmapDescriptorFactory.fromBitmap(customMarker)));
                            }

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onMapReady: map is ready");

                mMap = googleMap;
                //mMap.setOnPolylineClickListener(getActivity());

                if (mLocationPermissionsGranted) {

                    getDeviceLocation(true);

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);

                }
            }
        });

        return rootView;
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

        String runTimeData = String.valueOf((System.currentTimeMillis()-time)/1000);
        String dateData = String.valueOf(System.currentTimeMillis());
        // Saving new run data

        Bundle bundle = new Bundle();
        bundle.putString("DISTANCE",String.valueOf(distance));
        bundle.putString("AVERAGE_SPEED",String.valueOf(averageSpeed));
        bundle.putString("RUN_TIME",runTimeData);
        bundle.putString("DATE",dateData);

        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main,fragment).commit();

    }

    private void getDeviceLocation(final boolean isMoveCameraRequired) {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
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
                                mNoGpsSignal.setVisibility(View.INVISIBLE);
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
                                Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            // no gps
                            mNoGpsSignal.setVisibility(View.VISIBLE);
                        }

                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
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
                }
            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        Log.d(TAG, "onPolylineClick: getCameraPosition: " + mMap.getCameraPosition());
        Toast.makeText(getActivity(), "Route type " + polyline.getColor(), Toast.LENGTH_SHORT).show();

    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void captureScreen() {

        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                bitmap = snapshot;

                OutputStream fout = null;

                String filePath = System.currentTimeMillis() + ".jpeg";

                try {
                    fout = getActivity().openFileOutput(filePath, MODE_WORLD_READABLE);

                    // Write the string to the file
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                    fout.flush();
                    fout.close();
                }
                catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "FileNotFoundException");
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "IOException");
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }

                openShareImageDialog(filePath);
            }
        };

        mMap.snapshot(callback);
    }

    public void openShareImageDialog(String filePath)
    {
        File file = getActivity().getFileStreamPath(filePath);

        if(!filePath.equals("")) {

            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
        else {
            //This is a custom class I use to show dialogs...simply replace this with whatever you want to show an error message, Toast, etc.
           // DialogUtilities.showOkDialogWithText(this, R.string.shareImageFailed);
        }
    }

}
