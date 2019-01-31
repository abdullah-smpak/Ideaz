package com.example.mimmadjaved.ideazshuttle;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.mimmadjaved.ideazshuttle.Action.MyPREFERENCES;
import static com.example.mimmadjaved.ideazshuttle.Action.NAME;
import static com.example.mimmadjaved.ideazshuttle.Action.ID;

public class attendance extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static TextView usertxt;
    private static Button exit;
    private static Button OfficeIn;
    private static Button OfficeOut;
    public String idholder, officeholder, nameholder, longholder, latholder;
    String m_deviceId;
    GPSTracker gps;
    int checkin=1,checkout=1;
    SharedPreferences sharedpreferences;
    private ProgressDialog pDialog;
    Context mContext;
    JSONParser jsonParser = new JSONParser();

    String HttpURLin = "http://ideazshuttle.com/mobile_app/atrack_in.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String HttpURLout = "http://ideazshuttle.com/mobile_app/atrack_out.php";
    private static final String TAG_SSUCCESS = "success";
    private static final String TAG_MESSSAGE = "message";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double fusedLatitude = 0.0;
    private double fusedLongitude = 0.0;


    private GoogleMap mMap;
    DatabaseReference ref;
    GeoFire geoFire;

    private static final int MY_PERMISSION_REQUEST_CODE = 1234;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 321;

    private LocationRequest mLocationReuqest;
    private GoogleApiClient mGoogleApiClient1;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);


        OfficeIn.setEnabled(false);




        gps = new GPSTracker(attendance.this);
        if (gps.canGetLocation()) {
            if (checkPlayServices()) {
                startFusedLocation();
                registerRequestUpdate(this);
            }

        } else {
            gps.showSettingsAlert();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ref = FirebaseDatabase.getInstance().getReference("mylocation");
        geoFire = new GeoFire(ref);


        setUpLocation();


        usertxt = (TextView) findViewById(R.id.display_name);
        OfficeIn = (Button) findViewById(R.id.mark_in);
        OfficeOut = (Button) findViewById(R.id.mark_out);

        mContext = this;

        //Start IMEI

        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        m_deviceId = TelephonyMgr.getDeviceId();

        //End IMEI

        //Start SharedPreferences

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        usertxt.setText("Hello! " + sharedpreferences.getString(NAME, ""));
        nameholder = sharedpreferences.getString(NAME, "");
        idholder = sharedpreferences.getString(ID, "");

        OfficeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gps = new GPSTracker(attendance.this);
                if (gps.canGetLocation()) {


                    if (latholder == "0.0" || longholder == "0.0") {
                        Toast.makeText(attendance.this, "Please Check location is 'ON' or 'OFF'.. ", Toast.LENGTH_LONG).show();
                    } else {
                        new inattendance().execute();
                    }
                } else {
                    gps.showSettingsAlert();
                }


                //Toast.makeText(Attendence.this, " Office In -- ID:" + idholder + " LAT:" + latholder + " LONG:" + longholder , Toast.LENGTH_LONG).show();
            }
        });

        OfficeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gps = new GPSTracker(attendance.this);
                if (gps.canGetLocation()) {


                    if (latholder == "0.0" || longholder == "0.0") {
                        Toast.makeText(attendance.this, "Please Check location is 'ON' or 'OFF'.. ", Toast.LENGTH_LONG).show();
                    } else {
                        new outattendance().execute();
                    }
                } else {
                    gps.showSettingsAlert();
                }


                //   Toast.makeText(Attendence.this, " Office OUT -- ID:" + idholder + " LAT:" + latholder + " LONG:" + longholder , Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices1()) {
                        buildGoogleApiClient();
                        createLocationRequest();


                    }
                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices1()) {
                buildGoogleApiClient();
                createLocationRequest();
                display();
            }
        }

    }

    private void createLocationRequest() {
        mLocationReuqest = new LocationRequest();
        mLocationReuqest.setInterval(UPDATE_INTERVAL);
        mLocationReuqest.setFastestInterval(FASTEST_INTERVAL);
        mLocationReuqest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationReuqest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayServices1() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else {
                Toast.makeText(this, "Device Not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }


    private void display() {
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double lon = mLastLocation.getLongitude();
            //  Toast.makeText(this, latitude+" "+lon, Toast.LENGTH_SHORT).show();

            geoFire.setLocation("You", new GeoLocation(latitude, lon), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {


                    //Toast.makeText(getApplicationContext(), "asdasd", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        display();


    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationReuqest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        LatLng fence = new LatLng(24.918486, 67.090913);
        mMap.addCircle(new CircleOptions()
                .center(fence)
                .radius(75));


        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(fence.latitude, fence.longitude), 0.5f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Toast.makeText(attendance.this, "You are in Office", Toast.LENGTH_SHORT).show();


                if (checkin == 1) {
                    checkin =2;
                    gps = new GPSTracker(attendance.this);
                    if (gps.canGetLocation()) {


                        if (latholder == "0.0" || longholder == "0.0") {
                            // Toast.makeText(attendance.this, "Please Check location is 'ON' or 'OFF'.. ", Toast.LENGTH_LONG).show();
                        } else {
                            new inattendance().execute();
                            OfficeIn.setEnabled(false);

                        }
                    } else {
                        gps.showSettingsAlert();
                    }
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // This method will be executed once the timer is over
                            OfficeIn.setEnabled(true);
                            checkin = 1;
                            //Log.d(TAG,"resend1");

                        }
                    }, 53280000);// set time as per your requirement
                }
                else
                {

                }
            }



            @Override
            public void onKeyExited(String key) {
                Toast.makeText(attendance.this, "You are out of Office", Toast.LENGTH_SHORT).show();
                Calendar calendar1= Calendar.getInstance(Locale.getDefault());
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);


                int time = hour+minute;

                if(time>18)
                {
                    if (checkin == 1) {
                        checkin = 2;
                        gps = new GPSTracker(attendance.this);
                        if (gps.canGetLocation()) {


                            if (latholder == "0.0" || longholder == "0.0") {
                                Toast.makeText(attendance.this, "Please Check location is 'ON' or 'OFF'.. ", Toast.LENGTH_LONG).show();
                            } else {
                                new outattendance().execute();
                            }
                        } else {
                            gps.showSettingsAlert();
                        }


                        Toast.makeText(attendance.this, "Checked Out..", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // This method will be executed once the timer is over
                                OfficeIn.setEnabled(true);
                                checkin = 1;
                                //Log.d(TAG,"resend1");

                            }
                        }, 53280000);// set time as per your requirement

                    }
                }

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }


    class inattendance extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(attendance.this);
            pDialog.setMessage("Marking Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("UserId", idholder));
            params.add(new BasicNameValuePair("longitude", longholder));
            params.add(new BasicNameValuePair("latitude", latholder));
            params.add(new BasicNameValuePair("imei", m_deviceId));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(HttpURLin, "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    return json.getString(TAG_MESSAGE);
                } else {

                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String message1) {
            pDialog.dismiss();
            if (message1 != null) {
                Toast.makeText(attendance.this, message1, Toast.LENGTH_LONG).show();
            }
        }

    }

    class outattendance extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(attendance.this);
            pDialog.setMessage("Marking Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("UserId", idholder));

            params.add(new BasicNameValuePair("longitude", longholder));
            params.add(new BasicNameValuePair("latitude", latholder));


            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(HttpURLout, "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SSUCCESS);

                if (success == 1) {

                    return json.getString(TAG_MESSSAGE);
                } else {

                    return json.getString(TAG_MESSSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String message1) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (message1 != null) {
                Toast.makeText(attendance.this, message1, Toast.LENGTH_LONG).show();
            }
        }

    }

    protected void onStop() {
        stopFusedLocation();
        super.onStop();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(),
                        "This device is supported. Please download google play services", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void startFusedLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnectionSuspended(int cause) {
                        }

                        @Override
                        public void onConnected(Bundle connectionHint) {
                        }
                    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                        }
                    }).build();
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.connect();
        }
    }

    public void stopFusedLocation() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void registerRequestUpdate(final LocationListener listener) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // every second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!isGoogleApiClientConnected()) {
                        mGoogleApiClient.connect();
                    }
                    registerRequestUpdate(listener);
                }
            }
        }, 10);
    }

    public boolean isGoogleApiClientConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onLocationChanged(Location location) {


        mLastLocation = location;
        display();
        if (location != null) {
            setFusedLatitude(location.getLatitude());
            setFusedLongitude(location.getLongitude());


            latholder = "" + getFusedLatitude();
            longholder = "" + getFusedLongitude();
        } else {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Build the alert dialog
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Active");
                builder.setMessage("Please enable Location Services and GPS");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

            }
        }

    }

    public void setFusedLatitude(double lat) {
        fusedLatitude = lat;
    }

    public void setFusedLongitude(double lon) {
        fusedLongitude = lon;
    }

    public double getFusedLatitude() {
        return fusedLatitude;
    }

    public double getFusedLongitude() {
        return fusedLongitude;
    }


}
