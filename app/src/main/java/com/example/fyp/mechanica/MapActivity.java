package com.example.fyp.mechanica;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.MLocation;
import com.example.fyp.mechanica.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener{


    private boolean isPermGranted = false;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    private GoogleMap map;
    DatabaseReference dbRef;
    User currUser;
    String pushKey;
    MLocation mLocation = new MLocation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbRef = FirebaseDatabase.getInstance().getReference();
        currUser = Paper.book().read(Constants.CURR_USER_KEY);

        MapActivityPermissionsDispatcher.showMapWithPermissionCheck(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        if (currUser.userRole.equals("Customer")) {

            dbRef.child("lives").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MLocation location = snapshot.getValue(MLocation.class);

                        if (location != null) {
                            LatLng latLng = new LatLng(location.latitude, location.longitude);
                            map.addMarker(new MarkerOptions().position(latLng).title("Mechanic"));
                            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
//    private void initMap() {
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(GoogleMap googleMap) {
//                    map = googleMap;
//
//                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
//                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this,
//                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    map.setMyLocationEnabled(true);
//                }
//            });
//        }
//
//    }

//    public void getLocationPermission() {
//        String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION};
//
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FIND_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                    isPermGranted = true;
//
//            } else {
//                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE );
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        isPermGranted = false;
//
//        switch (requestCode) {
//            case LOCATION_PERMISSION_REQUEST_CODE:
//                if (grantResults.length > 0) {
//
//                    for (int i = 0; i < grantResults.length; i++) {
//                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                            isPermGranted = false;
//                            return;
//                        }
//                    }
//                }
//                    isPermGranted = true;
//                    // initialize map
//                        initMap();
//                }
//        }
//

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void showMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) MapActivity.this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
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
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            map.addMarker(new MarkerOptions().position(latLng).title("Me"));
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            mLocation.latitude = currentLatitude;
            mLocation.longitude = currentLongitude;
            if (pushKey == null) {
                pushKey = dbRef.child("lives").child(currUser.id).push().getKey();

            } else {
                dbRef.child("lives").child(currUser.id).child(pushKey).setValue(mLocation);
            }

            Log.d("TAG", String.valueOf(currentLatitude));
            Log.d("TAG", String.valueOf(currentLongitude));

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
         /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        mLocation.latitude = currentLatitude;
        mLocation.longitude = currentLongitude;

        if (pushKey == null) {
            pushKey = dbRef.child("lives").child(currUser.id).push().getKey();

        } else {
            dbRef.child("lives").child(currUser.id).child(pushKey).setValue(mLocation);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnMyLocationButtonClickListener(this);

    }

    @Override
    public boolean onMyLocationButtonClick() {

        return false;
    }
}
