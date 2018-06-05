package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Info extends AppCompatActivity {
    private static final int REQUEST_LOCATION=1;
    long LOCATION_REFRESH_TIME=333;
    float LOCATION_REFRESH_DISTANCE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);

        final LinearLayout linearLayout=(LinearLayout)findViewById(R.id.sent_info);

        Bundle extras=getIntent().getExtras();
        final int[] ia=extras.getIntArray("select");
        for(int i:ia){
            TextView textView=new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            linearLayout.addView(textView);
        }

        SensorManager manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        final ArrayList<Sensor> sensors=new ArrayList<>(manager.getSensorList(Sensor.TYPE_ALL));
        final ArrayList<Sensor> selected=new ArrayList<>();
        final ArrayList<String> sensorNames=new ArrayList<>();

        for(int i:ia){
            Sensor sensor=sensors.get(i);
            selected.add(sensor);
            sensorNames.add(sensorAdapter.sensorTypeToString(sensor.getType()));
        }

        JSONObject names=null;
        try{
            AssetManager assetManager=getAssets();
            InputStream is=assetManager.open("Test.json");
            String content="";
            InputStreamReader ir=new InputStreamReader(is);
            BufferedReader br=new BufferedReader(ir);
            String st;
            while ((st=br.readLine())!=null){
                content+=st;
            }
            System.out.println(content);
            names=new JSONObject(content);

        }catch (Exception e){
            e.printStackTrace();
        }

        final JSONObject final_names=names;

        for(int j=0;j<ia.length;j++){
            final int i=j;
            SensorEventListener listener=new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if(event.sensor.equals(selected.get(i))){
                        JSONArray array=new JSONArray();
                        JSONArray nameArray=null;
                        try{
                            nameArray=(JSONArray)final_names.get(sensorNames.get(i));
                            Log.i("shunqi",nameArray.toString());
                        }catch(JSONException e){
                            e.printStackTrace();
                            Log.i("shunqi","In Exception");
                        }
                        try{
                            for(int x=0;x<event.values.length;x++){
                                if(nameArray==null){
                                    array.put(new JSONObject().put(
                                            "Unknown",event.values[x]
                                    ));
                                }else{
                                    array.put(new JSONObject().put(
                                            (String)nameArray.get(x),event.values[x]
                                    ));
                                }
                            }
                            JSONObject object=new JSONObject();

                            object.put(sensorNames.get(i),array);
                            TextView text=(TextView)linearLayout.getChildAt(i);
                            text.setText(object.toString());
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

            manager.registerListener(listener,selected.get(i),333333);
        }

        for(int i=0;i<linearLayout.getChildCount();i++){
            TextView textView=(TextView)linearLayout.getChildAt(i);
            if(textView.getText().equals("")){
                textView.setText("No Data");
            }
        }

        final TextView locationText=(TextView)findViewById(R.id.location);

        final LocationListener locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location!=null){
                    String text="Latitude: "+location.getLatitude()
                            +"\nLongitude: "+location.getLongitude();
                    locationText.setText(text);
                }else {
                    Toast.makeText(getApplicationContext(),"Unable to get changing location",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };


        LocationManager mLocationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            Location location=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location!=null){
                String text="Latitude: "+location.getLatitude()
                        +"\nLongitude: "+location.getLongitude();
                locationText.setText(text);
            }else {
                Toast.makeText(this,"Unable to get location",Toast.LENGTH_LONG).show();
            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE,locationListener);
        }
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent event){
        if(key==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(key,event);
    }
}
