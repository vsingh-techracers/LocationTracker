package com.example.rails.myapplication;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rails on 27/9/16.
 */
public class GpsTracker extends Service implements LocationListener {

    private final Context context;
    boolean isNetworkEnabled = false;
    boolean isGPSEnabled = false;
    boolean canGetLocation = false;
    Location location;
    private final String url;
    double longitude;
    double latitude;
    double altitude;
    private long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static int  x= 0;
    private long MIN_TIME_BW_UPDATES;
    protected LocationManager locationManager;

    public GpsTracker(Context context, long timeInterval, String url) {
        this.context = context;
        this.url = url;
        MIN_TIME_BW_UPDATES = timeInterval;
        getLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String altitude = String.valueOf(location.getAltitude());
        String accuracy = String.valueOf(location.getAccuracy());
        String extra = String.valueOf(location.getExtras());
        String provider = String.valueOf(location.getProvider());
        String speed = String.valueOf(location.getSpeed());
        String bearing = String.valueOf(location.getBearing());
        String time = String.valueOf(location.getTime());
        JSONObject postDataParams = new JSONObject();
        try {
            postDataParams.put("latitude", latitude);
            postDataParams.put("longitude", longitude);
            postDataParams.put("altitude", altitude);
            postDataParams.put("altitude", accuracy);
            postDataParams.put("altitude", extra);
            postDataParams.put("altitude", provider);
            postDataParams.put("altitude", speed);
            postDataParams.put("altitude", bearing);
            postDataParams.put("altitude", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SendPostRequest spr = new SendPostRequest(postDataParams);
        spr.execute(url);
        Toast.makeText(context, "Data Sent " + (++x), Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, "Status " + ress, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                    }


                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }

                        }
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }


    public void stopUsingGPS() {
        if (locationManager != null) {

            locationManager.removeUpdates(GpsTracker.this);
        }
    }


    public double getLatitude(){
        if(location!=null){
            latitude= location.getLatitude();
        }
        return latitude;
    }

    public double getAltitude(){
        if(location!=null){
            altitude= location.getAltitude();
        }
        return latitude;
    }

    public double getLongitude(){
        if(location!=null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation(){
        return this.canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not Enabled. Do You want to go to settings Menu?");
        alertDialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent =new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUsingGPS();
    }
}