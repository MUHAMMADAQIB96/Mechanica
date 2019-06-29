package com.example.fyp.mechanica;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.helpers.Helper;
import com.example.fyp.mechanica.models.ActiveJob;
import com.example.fyp.mechanica.models.Customer;
import com.example.fyp.mechanica.models.MLocation;
import com.example.fyp.mechanica.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MechanicMapActivity extends BaseDrawerActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {


    @BindView(R.id.btn_accept_request) Button btnAcceptRequest;
    @BindView(R.id.btn_vehicle_detail) Button btnVehicleDetail;

    @BindView(R.id.btn_arrived_for_work) Button btnArrivedForWork;
    @BindView(R.id.btn_call_customer) Button btnCallCustomer;

    @BindView(R.id.ll_arrived_for_work) LinearLayout llArrivedForWork;
    @BindView(R.id.ll_request) LinearLayout llAcceptRequest;

    @BindView(R.id.tv_address) TextView tvAddress;
    @BindView(R.id.tv_mile_away) TextView tvMileAway;
    @BindView(R.id.tv_km) TextView tvKM;

    @BindView(R.id.civ_user_photo) CircleImageView civPhoto;
    @BindView(R.id.tv_name) TextView tvCustomerName;
    @BindView(R.id.tv_vehicle_info) TextView tvVehicleInfo;
    @BindView(R.id.tv_cust_min_away) TextView tvCustomerMinAway;
    @BindView(R.id.tv_cust_km) TextView tvCustomerKM;


    private boolean isPermGranted = false;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private double currentLatitude;
    private double currentLongitude;

    DatabaseReference dbRef;
    User currUser, customer;
    String customerUID;

    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    private FusedLocationProviderClient fusedLocationClient;

    MLocation mLocation;

    AlertDialog dialog;
    ActionBar bar;

    String phoneNumber;
    ActiveJob activeJob = new ActiveJob();
    ValueEventListener jobRequestEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_map);
        ButterKnife.bind(this);

        dbRef = FirebaseDatabase.getInstance().getReference();
        currUser = Paper.book().read(Constants.CURR_USER_KEY);

        bar = getSupportActionBar();
        if (bar != null)
            bar.setTitle("Home");

        MechanicMapActivityPermissionsDispatcher.showMapWithPermissionCheck(MechanicMapActivity.this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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

    ValueEventListener requestEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    bar.setTitle("Job");
                    customerUID = snapshot.getKey();
                    getCustomerData(snapshot.getKey());
                    MLocation mLocation = snapshot.getValue(MLocation.class);
                    if (mLocation != null) {

                        Customer customer = new Customer();
                        customer.id = snapshot.getKey();
                        customer.lat = mLocation.latitude;
                        customer.lng = mLocation.longitude;

                        Paper.book().write(Constants.CUSTOMER_KEY, customer);

                        getAddress(mLocation.latitude, mLocation.longitude);
                        double distance = distance(currentLatitude, currentLongitude,
                                mLocation.latitude, mLocation.longitude);

                        llAcceptRequest.setVisibility(View.VISIBLE);

                        tvKM.setText(distance + "KM");
                        tvMileAway.setText("Your customer is " + Helper.getDurationOfDistance(distance) + " minutes away");

                    }

                }

            } else {
                llAcceptRequest.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        jobRequestEventListener =  new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        bar.setTitle("Job");
                        customerUID = snapshot.getKey();
                        getCustomerData(snapshot.getKey());
                        MLocation mLocation = snapshot.getValue(MLocation.class);
                        if (mLocation != null) {

                            Customer customer = new Customer();
                            customer.id = snapshot.getKey();
                            customer.lat = mLocation.latitude;
                            customer.lng = mLocation.longitude;

                            Paper.book().write(Constants.CUSTOMER_KEY, customer);

                            getAddress(mLocation.latitude, mLocation.longitude);
                            double distance = distance(currentLatitude, currentLongitude,
                                    mLocation.latitude, mLocation.longitude);

                            llAcceptRequest.setVisibility(View.VISIBLE);

                            tvKM.setText(distance + "KM");
                            tvMileAway.setText("Your customer is " + Helper.getDurationOfDistance(distance) + " minutes away");

                        }

                    }

                } else {
                    llAcceptRequest.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        dbRef.child("requests").addValueEventListener(jobRequestEventListener);

        getActiveJobInfo();
        getJobStatus();

    }


    public void removedJobRequestListener() {
        dbRef.child("requests").removeEventListener(jobRequestEventListener);

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
                    MechanicMapActivity.this);

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
        } else {
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
            if (super.isOnline) {
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
        }

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

//        //Place current location marker
//
    }


    @OnClick(R.id.btn_accept_request)
    public void setBtnAcceptRequest() {

        Log.d("IRFAN", "Working");

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

//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (location == null) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
//                    MechanicMapActivity.this);
//
//        } else {
//            ActiveJob activeJob = new ActiveJob();
//            activeJob.mechanicID = currUser.id;
//            activeJob.mechLat = location.getLatitude();
//            activeJob.mechLon = location.getLongitude();
//            activeJob.startedAt = (new Date()).getTime();
//
//            Customer customer = Paper.book().read(Constants.CUSTOMER_KEY);
//            activeJob.customerID = customer.id;
//            activeJob.cusLon = customer.lng;
//            activeJob.cusLat = customer.lat;
//
//            dbRef.child("activeJobs").push().setValue(activeJob);
//        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("IRFAN", "Location is: " + location);
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            ActiveJob activeJob = new ActiveJob();
                            activeJob.mechanicID = currUser.id;
                            activeJob.mechLat = location.getLatitude();
                            activeJob.mechLon = location.getLongitude();
                            activeJob.startedAt = (new Date()).getTime();

                            Customer customer = Paper.book().read(Constants.CUSTOMER_KEY);
                            activeJob.customerID = customer.id;
                            activeJob.cusLon = customer.lng;
                            activeJob.cusLat = customer.lat;

                            dbRef.child("activeJobs").push().setValue(activeJob);
                        }
                    }
                });


        dbRef.child("requests").child(customer.id).removeValue();
        removedJobRequestListener();

        llAcceptRequest.setVisibility(View.GONE);
        llArrivedForWork.setVisibility(View.VISIBLE);

    }


    @OnClick(R.id.btn_vehicle_detail)
    public void setBtnVehicleDetail() {

        AlertDialog builder = new AlertDialog.Builder(this).create();

        View view = View.inflate(this, R.layout.vehicle_info_dialog, null);
        TextView vehicle = view.findViewById(R.id.tv_vehicle);
        TextView registrationNo = view.findViewById(R.id.tv_registration_no);
        TextView tvColor = view.findViewById(R.id.tv_color);
        TextView tvModel = view.findViewById(R.id.tv_model);

        builder.setView(view);

        builder.show();

        if (customer != null) {
           vehicle.setText(customer.vehicle.type);
           registrationNo.setText(customer.vehicle.registrationNo);
           tvColor.setText(customer.vehicle.color);
           tvModel.setText(customer.vehicle.model);
        }
    }


    @OnClick (R.id.btn_arrived_for_work)
    public void setBtnArrivedForWork() {
        dbRef.child("activeJobs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ActiveJob job = snapshot.getValue(ActiveJob.class);
                        if (job != null  && job.mechanicID.equals(currUser.id)) {
                            String jobId = snapshot.getKey();

                            if (jobId != null)
                                dbRef.child("activeJobs").child(jobId).child("jobStatus").setValue(1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @OnClick(R.id.btn_call_customer)
    public void setBtnCallCustomer() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        this.startActivity(intent);
    }


    public void getActiveJobInfo() {
        dbRef.child("activeJobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ActiveJob job = snapshot.getValue(ActiveJob.class);
                        if (job != null && currUser.id.equals(job.mechanicID)) {

                            removedJobRequestListener();

                            llArrivedForWork.setVisibility(View.VISIBLE);
                            getCustomerData(job.customerID);


                            double distance = distance(job.cusLat, job.cusLon, job.mechLat, job.mechLon);

                            tvCustomerKM.setText(distance + "KM");
                            tvCustomerMinAway.setText("Your customer is " +
                                    Helper.getDurationOfDistance(distance) + " minutes away");

                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getCustomerData(String uid) {
        dbRef.child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        customer = user;
                        phoneNumber = user.phoneNumber;
                        tvCustomerName.setText(user.name);
                        tvVehicleInfo.setText(user.vehicle.type + " | " + user.vehicle.registrationNo);

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
                        if (job != null && job.mechanicID.equals(currUser.id)) {
                            removedJobRequestListener();

                            if (job.jobStatus == 2) {
                                Intent intent = new Intent(MechanicMapActivity.this,
                                        JobStartedActivity.class);
                                intent.putExtra("JOB_ID", snapshot.getKey());
                                intent.putExtra("JOB", job);
                                startActivity(intent);
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
        Geocoder geocoder = new Geocoder(MechanicMapActivity.this, Locale.getDefault());
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


}
