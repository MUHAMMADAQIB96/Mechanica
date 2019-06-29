package com.example.fyp.mechanica;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.helpers.Helper;
import com.example.fyp.mechanica.models.ActiveJob;
import com.example.fyp.mechanica.models.LiveMechanic;
import com.example.fyp.mechanica.models.MLocation;
import com.example.fyp.mechanica.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MapActivity extends BaseDrawerActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {
//         RoutingListener {s

    @BindView(R.id.ll_request) LinearLayout llRequest;
    @BindView(R.id.ll_cancel_request) LinearLayout llCancelRequest;
    @BindView(R.id.btn_cancel_request) Button btnCancelRequest;

    @BindView(R.id.tv_address) TextView tvAddress;
    @BindView(R.id.cv_address) CardView cvAddress;

    @BindView(R.id.btn_request) Button btnRequest;
    @BindView(R.id.btn_confirm_location) Button btnConfirmLocation;
    @BindView(R.id.tv_finding_mechanic) TextView tvFindingMechanic;

    @BindView(R.id.ll_mechanic_card) LinearLayout llMechanicCard;
    @BindView(R.id.tv_cust_km) TextView tvKMAway;
    @BindView(R.id.tv_cust_min_away) TextView tvMinAway;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_profession) TextView tvProfession;

    @BindView(R.id.ll_mechanic_arrived_action_btns) LinearLayout llMechanicArrivedActionBtns;
    @BindView(R.id.btn_call_mechanic) Button btnCallMechanic;
    @BindView(R.id.btn_confirm_mechanic_request) Button btnConfirmMechanicRequest;
    @BindView(R.id.btn_call_mech) Button btnCallMech;

    private boolean isPermGranted = false;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    //    private GoogleApiClient mGoogleApiClient;
//    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    DatabaseReference dbRef;
    User currUser;

    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    HashMap<String, Marker> mechMarkers = new HashMap<>();

    String phoneNumber;

    MLocation mLocation;

    AlertDialog dialog;
    ActionBar bar;

    private List<Polyline> polylines;
    LatLng start;
    LatLng end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        dbRef = FirebaseDatabase.getInstance().getReference();
        currUser = Paper.book().read(Constants.CURR_USER_KEY);

        bar = getSupportActionBar();
        if (bar != null)
            bar.setTitle("Select Your Location");

        MapActivityPermissionsDispatcher.showMapWithPermissionCheck(this);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                // The next two lines tell the new client that “this” current class will handle connection stuff
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                //fourth line adds the LocationServices API endpoint from GooglePlayServices
//                .addApi(LocationServices.API)
//                .build();
//
//        // Create the LocationRequest object
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
//                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        if (currUser.userRole.equals("Customer")) {

            dbRef.child("lives").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    LiveMechanic mechanic = dataSnapshot.getValue(LiveMechanic.class);
                    if (mechanic != null) {
                        double distance = distance(mechanic.latitude, mechanic.longitude,
                                currentLatitude, currentLongitude);

                        LatLng latLng = new LatLng(mechanic.latitude, mechanic.longitude);
                        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mechanic_maker_48_ic))
                                .title(distance + " KM"));

                        mechMarkers.put(dataSnapshot.getKey(), marker);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    LiveMechanic mechanic = dataSnapshot.getValue(LiveMechanic.class);
                    if (mechanic != null) {
                        double distance = distance(mechanic.latitude, mechanic.longitude,
                                currentLatitude, currentLongitude);
                        LatLng latLng = new LatLng(mechanic.latitude, mechanic.longitude);

                        if (mechMarkers.containsKey(dataSnapshot.getKey())) {
                            Marker marker = mechMarkers.get(dataSnapshot.getKey());
                            marker.setTitle(distance + " KM");
                            marker.setPosition(latLng);
                        }
                        else {
                            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mechanic_maker_48_ic))
                                    .title(distance + " KM"));
                            mechMarkers.put(dataSnapshot.getKey(), marker);
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    if (mechMarkers.containsKey(dataSnapshot.getKey())) {
                        Marker marker = mechMarkers.remove(dataSnapshot.getKey());
                        marker.remove();
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        polylines = new ArrayList<>();

        start = new LatLng(24.9128302, 67.0920466);
        end = new LatLng(24.8707794, 67.3553665);

//        Routing routing = new Routing.Builder()
//                .travelMode(AbstractRouting.TravelMode.DRIVING)
//                .withListener(this)
//                .alternativeRoutes(true)
//                .waypoints(start, end)
//                .key(getResources().getString(R.string.google_map_api_key))
//                .build();
//
//        routing.execute();


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
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //Now lets connect to the API
//        mGoogleApiClient.connect();
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) MapActivity.this);
//            mGoogleApiClient.disconnect();
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRequestsResponse();
        getJobStatus();
//        if (currUser.userRole.equals("Customer")) {
//            btnRequest.setVisibility(View.VISIBLE);
//
//        } else {
//            btnRequest.setVisibility(View.GONE);
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            final View view = getLayoutInflater().inflate(R.layout.dialog_request, null);
//            TextView tvUserName = view.findViewById(R.id.tv_username);
//            TextView tvMsg = view.findViewById(R.id.tv_msg);
//
//            builder.setView(view);
//            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Job job = new Job();
//                    job.customerLat = mLocation.latitude;
//                    job.customerLng = mLocation.longitude;
//                    job.customerUID = mLocation.uid;
//                    job.mechanicUID = currUser.id;
//
//                    Date date = new Date();
//                    job.startAt = date.getTime();
//
//                    dbRef.child("jobs").child(currUser.id).setValue(job);
//                    dbRef.child("request").removeValue();
//                }
//            });
//
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//
//                }
//            });
//
//            final AlertDialog dialog = builder.create();
//
//            dbRef.child("lives").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        String id = snapshot.getKey();
//                        if (currUser.id.equals(id)) {
//
//                            dbRef.child("request").addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    mLocation = dataSnapshot.getValue(MLocation.class);
//                                    if (mLocation != null) {
//
//                                        if (mLocation.uid == null) {
//                                            dialog.dismiss();
//
//                                        } else {
//                                            dbRef.child("users").child(mLocation.uid).addListenerForSingleValueEvent(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                    User user = dataSnapshot.getValue(User.class);
//                                                    ((TextView) view.findViewById(R.id.tv_username)).setText(user.name);
//                                                    ((TextView) view.findViewById(R.id.tv_msg)).setText(user.name + " send you a job request, do you want to accept it?");
//
//                                                    dialog.show();
//                                                }
//
//                                                @Override
//                                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                                }
//                                            });
//                                        }
//
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                }
//                            });
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//
//        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                    this);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                    MapActivity.this);

        } else {
            if (currUser.userRole.equals("Mechanic")) {

                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("My Location");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                MLocation mLocation = new MLocation();
                mLocation.latitude = currentLatitude;
                mLocation.longitude = currentLongitude;

                dbRef.child("lives").child(currUser.id).setValue(mLocation);
            }
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


//    @OnClick(R.id.btn_request)
    public void setBtnRequest() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);

        mBuilder.setTitle("Send Request");
        mBuilder.setMessage("Send Request to the Nearest Mechanics");

        mBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapActivity.this);

                } else {
                    MLocation mLocation = new MLocation();

                    mLocation.longitude = location.getLongitude();
                    mLocation.latitude = location.getLatitude();
                    mLocation.uid = currUser.id;

                    dbRef.child("request").setValue(mLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MapActivity.this, "Your request has been sent", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });

        dialog = mBuilder.create();
        dialog.show();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
//        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mGoogleMap.setOnMyLocationButtonClickListener(this);

            } else {
                //Request Location Permission
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        MLocation mLocation = new MLocation();
        mLocation.latitude = currentLatitude;
        mLocation.longitude = currentLongitude;

        if (currUser.userRole.equals("Mechanic")) {
            dbRef.child("lives").child(currUser.id).setValue(mLocation);

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

            //move map camera
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

//        //Place current location marker
//
    }


    @Override
    public boolean onMyLocationButtonClick() {

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        return false;
    }


    @OnClick(R.id.btn_confirm_location)
    public void confirmLocation() {
        btnRequest.setVisibility(View.VISIBLE);
        btnConfirmLocation.setVisibility(View.GONE);

        bar.setTitle("Request Mechanic");

        cvAddress.setVisibility(View.VISIBLE);
        getAddress(currentLatitude, currentLongitude);
    }


    @OnClick(R.id.btn_request)
    public void requestMechanic() {
        tvFindingMechanic.setVisibility(View.VISIBLE);
        llCancelRequest.setVisibility(View.VISIBLE);
        btnRequest.setVisibility(View.GONE);
        cvAddress.setVisibility(View.GONE);

        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                    MapActivity.this);

        } else {
            MLocation mLocation = new MLocation();

            mLocation.longitude = location.getLongitude();
            mLocation.latitude = location.getLatitude();
//            mLocation.uid = currUser.id;
            mLocation.requestAt = (new Date()).getTime();

            dbRef.child("requests").child(currUser.id).setValue(mLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MapActivity.this, "Your request has been sent",
                            Toast.LENGTH_SHORT).show();

                }
            });

        }
    }

    @OnClick(R.id.btn_cancel_request)
    public void cancelRequest() {
        dbRef.child("requests").child(currUser.id).removeValue();
        btnConfirmLocation.setVisibility(View.VISIBLE);
        tvFindingMechanic.setVisibility(View.GONE);
        llCancelRequest.setVisibility(View.GONE);
    }

    public void checkRequestsResponse() {
        dbRef.child("activeJobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ActiveJob job = snapshot.getValue(ActiveJob.class);
                        if (job != null && job.customerID.equals(currUser.id)) {

                            llRequest.setVisibility(View.GONE);
                            tvFindingMechanic.setText("Your mechanic is on way...");
                            llCancelRequest.setVisibility(View.GONE);
                            llMechanicCard.setVisibility(View.VISIBLE);

                            double distance = distance(job.cusLat, job.cusLon, job.mechLat, job.mechLon);
                            int time = Helper.getDurationOfDistance(distance);
                            getMechanicInfo(job.mechanicID);
                            tvMinAway.setText("Your Mechanic is " + time + " minutes away");
                            tvKMAway.setText(distance + " KM");

                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getJobStatus() {
        dbRef.child("activeJobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ActiveJob job = snapshot.getValue(ActiveJob.class);
                        if (job != null && job.customerID.equals(currUser.id)) {
//                            String jobId = snapshot.getKey();
                            if (job.jobStatus == 1) {
                                llMechanicArrivedActionBtns.setVisibility(View.VISIBLE);
                                btnCallMech.setVisibility(View.GONE);
                            }
                            else if (job.jobStatus == 2) {
                                startActivity(new Intent(MapActivity.this, JobStartedActivity.class));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getMechanicInfo(String uid) {
        dbRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        tvName.setText(user.name);
                        phoneNumber = user.phoneNumber;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @OnClick(R.id.btn_call_mech)
    public void setBtnCallMech() {
        callToMechanic(phoneNumber);
    }


    @OnClick(R.id.btn_call_mechanic)
    public void setBtnCallMechanic() {
        callToMechanic(phoneNumber);
    }


    public void callToMechanic(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        this.startActivity(intent);
    }


    @OnClick(R.id.btn_confirm_mechanic_request)
    public void setBtnConfirmMechanicRequest() {
        dbRef.child("activeJobs")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ActiveJob job = snapshot.getValue(ActiveJob.class);
                        if (job!= null && job.customerID.equals(currUser.id)) {
                            String jobId = snapshot.getKey();

                            dbRef.child("activeJobs").child(jobId).child("jobStatus").setValue(2);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        startActivity(new Intent(MapActivity.this, JobStartedActivity.class));


    }

//
//    private double distance(double lat1, double lon1, double lat2, double lon2) {
//        double theta = lon1 - lon2;
//        double dist = Math.sin(deg2rad(lat1))
//                * Math.sin(deg2rad(lat2))
//                + Math.cos(deg2rad(lat1))
//                * Math.cos(deg2rad(lat2))
//                * Math.cos(deg2rad(theta));
//        dist = Math.acos(dist);
//        dist = rad2deg(dist);
//        dist = dist * 60 * 1.1515;
//
//        return milesIntoKiloMeter(dist);
////        return (dist);
//    }
//
//    private double deg2rad(double deg) {
//        return (deg * Math.PI / 180.0);
//    }
//
//    private double rad2deg(double rad) {
//        return (rad * 180.0 / Math.PI);
//    }
//
//    private double milesIntoKiloMeter(double miles) {
//        double km = miles / 0.62137;
//
//        return Math.round(km * 10) / 10.0;
//    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        return milesIntoKiloMeter(dist);
//        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double milesIntoKiloMeter(double miles) {
        double km = miles / 0.62137;

        return Math.round(km * 10) / 10.0;
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            tvAddress.setText(add);

//            add = add + "\n" + obj.getCountryName();
//            add = add + "\n" + obj.getCountryCode();
//            add = add + "\n" + obj.getAdminArea();
//            add = add + "\n" + obj.getPostalCode();
//            add = add + "\n" + obj.getSubAdminArea();
//            add = add + "\n" + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


//
//    @Override
//    public void onRoutingFailure(RouteException e) {
//        if(e != null) {
//            Log.d("IRFAN", e.getMessage());
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }else {
//            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onRoutingStart() {
//
//    }
//
//    @Override
//    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
//        Toast.makeText(this, "Routing success", Toast.LENGTH_SHORT).show();
//        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
//
//        mGoogleMap.moveCamera(center);
//
//
//        if(polylines.size()>0) {
//            for (Polyline poly : polylines) {
//                poly.remove();
//            }
//        }
//
//        polylines = new ArrayList<>();
//        //add route(s) to the map.
////        for (int i = 0; i <route.size(); i++) {
////
////            //In case of more than 5 alternative routes
////            int colorIndex = i % COLORS.length;
////
////            PolylineOptions polyOptions = new PolylineOptions();
////            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
////            polyOptions.width(10 + i * 3);
////            polyOptions.addAll(route.get(i).getPoints());
////            Polyline polyline = map.addPolyline(polyOptions);
////            polylines.add(polyline);
////
////            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
////        }
//
//        // Start marker
//        MarkerOptions options = new MarkerOptions();
//        options.position(start);
//        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
////        options.icon(BitmapDescriptorFactory.fromResource());
//        mGoogleMap.addMarker(options);
//
//        // End marker
//        options = new MarkerOptions();
//        options.position(end);
//        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
////        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
//        mGoogleMap.addMarker(options);
//
//
//    }
//
//    @Override
//    public void onRoutingCancelled() {
//
//    }
//

}
