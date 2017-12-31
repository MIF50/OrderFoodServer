package mif50.com.orderfoodsserver;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
//import android.location.LocationListener;
import com.google.android.gms.location.LocationListener;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.Common;
import common.DirectionJSONParser;
import remote.IGeoCoordinates;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST=1000;
    private static final int LOCATION_PERMISSION_REQUEST=1001;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL=1000;
    private static final int FATEST_INTERVAL=5000;
    private static final int DISPLACEMENT=10;

    private IGeoCoordinates mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestRunTimePermission();
        }else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }
        displayLocation();

        //
        mService= Common.getGeoCodeService();
    }
    /**/
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
           requestRunTimePermission();
        }else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation!=null){
                double latitude = mLastLocation.getLatitude();
                double longitude= mLastLocation.getLongitude();
                // add marker in your location and move the camera
                LatLng yourLocation = new LatLng(latitude,longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("your location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                // After Add Marker for your location ,Add Marker for this Order and draw route
                drawRoute(yourLocation,Common.currentRequest.getAddress());
            }else {
                Toast.makeText(this, "could not get your location", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**/
    private void drawRoute(final LatLng yourLocation, String address) {
        mService.getGeoCode(address)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String lat =((JSONArray) jsonObject.get("results"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lat").toString();
                            String lng=((JSONArray)jsonObject.get("results"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lng").toString();
                            LatLng orderLocation=new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                            //
                            Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.order);
                            bitmap=Common.scaleBitmap(bitmap,70,70);
                            MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory
                            .fromBitmap(bitmap)).title("order of "+Common.currentRequest.getPhone())
                                    .position(orderLocation);
                            mMap.addMarker(marker);
                            // draw route
                            mService.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                                    orderLocation.latitude+" ,"+orderLocation.longitude)
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            new ParserTask().execute(response.body().toString());
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
    }

    /**/
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**/
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**/
    private boolean checkPlayServices() {
        int requestCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (requestCode!=ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(requestCode)){
                GooglePlayServicesUtil.getErrorDialog(requestCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(this, "this device not support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**/
    private void requestRunTimePermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    /*this method to move to this activity */
    public static Intent newIntent(Context context){
        Intent intent=new Intent(context,OrderTracking.class);
        return intent;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }
    /**/
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
           return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }
    // this method to listener => com.google.android.gms.location.LocationListener
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /**/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
    }

    //

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>{
        ProgressDialog dialog=new ProgressDialog(OrderTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("please wait ...");
            dialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes=null;
            try{
                jsonObject=new JSONObject(strings[0]);
                DirectionJSONParser parse = new DirectionJSONParser();
                routes=parse.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);
            dialog.dismiss();
            ArrayList points =null;
            PolylineOptions lineOption=null;
            for (int i=0;i<lists.size();i++){
                points=new ArrayList();
                lineOption=new PolylineOptions();
                List<HashMap<String,String>> path=lists.get(i);
                for (int j=0;j<path.size();j++){
                    HashMap<String,String> point= path.get(j);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);
                    points.add(position);
                }
                lineOption.addAll(points);
                lineOption.width(12);
                lineOption.color(Color.BLUE);
                lineOption.geodesic(true);
            }
            mMap.addPolyline(lineOption);

        }
    }
}

