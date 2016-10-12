package com.example.rails.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
public class MainActivity extends AppCompatActivity implements LocationListener {

    private EditText userNameEditText, urlEditText, timeIntervalEditText;
    private Button showMapButton,submitPostButton,stopButton;
    private TextView locationData ,responseTextView;
    private static int setDataAck;
    public static final String DEFAULT = "N/A";
    protected LocationManager locationManager;
    protected Context context;
    private static final int MY_ACCESS_FINE_LOCATION = 1;
    private static GpsTracker gps;
    private static int startCounter = 0;
    private static int stopCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userNameEditText = (EditText)findViewById(R.id.userNameEditText);
        urlEditText = (EditText)findViewById(R.id.urlEditText);
        timeIntervalEditText =(EditText)findViewById(R.id.timeIntervalEditText);
        locationData = (TextView)findViewById(R.id.locationDataTextView);
        responseTextView= (TextView)findViewById(R.id.responseTextView);
        responseTextView.setText("No Processing...");
        init();
    }
    private void validation() throws UnsupportedEncodingException {
        String typedUrl = urlEditText.getText().toString();
        String typedName = userNameEditText.getText().toString();

            String data = URLEncoder.encode("name", "UTF-8")
                    + "=" + URLEncoder.encode(typedName, "UTF-8");
        String text = "";
            BufferedReader reader=null;
            try
            {
                URL url = new URL(typedUrl);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write( data );
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }


                text = sb.toString();
            }
            catch(Exception ex)
            {

            }
            finally
            {
                try
                {

                    reader.close();
                }

                catch(Exception ex) {}
            }

            // Show response on activity
        Toast.makeText(MainActivity.this, " response = " + text, Toast.LENGTH_SHORT).show();



    }

    private void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_ACCESS_FINE_LOCATION );

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        if(setDataAck>0){
            SharedPreferences sharedpreferences = getSharedPreferences("GPS_User_Data", Context.MODE_PRIVATE);
            String savedUsername = sharedpreferences.getString("Username",DEFAULT );
            String savedUrl = sharedpreferences.getString("Url",DEFAULT);
            String savedTimeInterval = sharedpreferences.getString("TimeInterval",DEFAULT);
            if(savedUsername.equals(DEFAULT)|| savedUrl.equals(DEFAULT)||savedTimeInterval.equals(DEFAULT)){
                Toast.makeText(this, "No Data was found", Toast.LENGTH_SHORT).show();
            }else{
                userNameEditText.setText(savedUsername);
                urlEditText.setText(savedUrl);
                timeIntervalEditText.setText(savedTimeInterval);
            }
        }

        showMapButton = (Button)findViewById(R.id.showMapButton);
        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
                Toast.makeText(MainActivity.this, "It is in Progress..", Toast.LENGTH_SHORT).show();
            }
        });

        submitPostButton = (Button)findViewById(R.id.submitButton);
        submitPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    validation();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startCounter++;
                if(startCounter>1||urlEditText.getText().toString().equals("")||timeIntervalEditText.getText().toString().equals("")
                     ||userNameEditText.getText().toString().equals("")/*||!urlEditText.getText().toString().equals("http://requestb.in/16wbq7m1")  */ ){
                    startCounter=0;
                    Toast.makeText(MainActivity.this, " Multiple clicked Or Data Error!!! ", Toast.LENGTH_SHORT).show();
                }else{
                    stopCounter=0;
                    responseTextView.setText("Processing.....");
                    boolean setdata = true;
                    int ack = setData(setdata);
                    if (ack>0){
                        Toast.makeText(MainActivity.this, "Data was Successfully Saved!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Sorry ! Data was not saved.. try again..", Toast.LENGTH_SHORT).show();
                    }
                    String url; long timeInterval;
                    url = urlEditText.getText().toString();
                    timeInterval = 1000 * Integer.parseInt(timeIntervalEditText.getText().toString());
                    gps = new GpsTracker(MainActivity.this,timeInterval , url);
                    if(gps.canGetLocation()){
                        String latitude = String.valueOf(gps.getLatitude());
                        String longitude = String.valueOf(gps.getLongitude());
                        String altitude = String.valueOf(gps.getAltitude());

                        locationData.setText(String.format("Latitude : %s\nLongitude : %s\nAltitude : %s", latitude, longitude, altitude));
                    }
                    else {
                        gps.showSettingsAlert();
                    }
                    startService(new View(getApplicationContext()));

                }
            }
        });

        stopButton = (Button)findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCounter++;
                if(stopCounter>1||startCounter==0){

                    Toast.makeText(MainActivity.this, " Already Stopped!", Toast.LENGTH_SHORT).show();
                   responseTextView.setText("Already Stopped");
                }else{
                    startCounter =0;
                    gps.stopUsingGPS();
                    stopService(new View(getApplicationContext()));
                    responseTextView.setText("No Processing...");
                    Toast.makeText(MainActivity.this, "Tracking Location is stopped!", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case MY_ACCESS_FINE_LOCATION :
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

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

    public int setData(boolean shouldSetData){
        setDataAck = 0;
        if(shouldSetData==true){

            SharedPreferences sharedpreferences = getSharedPreferences("GPS_User_Data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("Username",userNameEditText.getText().toString());
            editor.putString("Url",urlEditText.getText().toString());
            editor.putString("TimeInterval",timeIntervalEditText.getText().toString());
            editor.apply();
            setDataAck =1;
        }
        return setDataAck;
    }


    // Method to start the service
    public void startService(View view) {
        startService(new Intent(getBaseContext(), GpsTracker.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), GpsTracker.class));
    }
}
