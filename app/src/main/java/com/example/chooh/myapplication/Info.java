package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.text.method.ScrollingMovementMethod;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


import java.util.HashMap;
import java.util.Map;

public class Info extends AppCompatActivity {
    private static final int REQUEST_LOCATION=1;
    long LOCATION_REFRESH_TIME=333;
    float LOCATION_REFRESH_DISTANCE=1;

    private SensorEventListener listener;
    private SensorManager manager;
    private final Map<Integer,Long> time_map=new HashMap<>();

    private final int interval=200;

    private MqttAndroidClient mqttClient;
    private MqttConnectOptions mqttOptions;

    private static String configName;
    private static String TAG = "MainActivity";
    private static String MQTT_URL = "ssl://tb.hpe-innovation.center:8883";
    private static String server="";
    private static String port="";
    private static String CLIENT_KEYSTORE_PASSWORD = "P@ssw0rd";
    private static String channel;

    private String id;
    private TelephonyManager telephonyManager;

    boolean bool = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);

        Bundle extras=getIntent().getExtras();

        final int[] ia=extras.getIntArray("select");
        configName=extras.getString("configName");

        File dir=this.getFilesDir();
        final File file=new File(dir,"config");

        String configContent="";
        try{
            BufferedReader br=new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine())!=null){
                configContent+=line;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        JSONArray array=null;
        try{
            array=new JSONArray(configContent);
        }catch (JSONException e){
            e.printStackTrace();
        }
        if(array!=null){
            for(int i=0;i<array.length();i++){
                try{
                    JSONObject o=array.getJSONObject(i);
                    if(o.getString("name").equals(configName)){
                        server=o.getString("server");
                        port=o.getString("port");
                        CLIENT_KEYSTORE_PASSWORD=o.getString("pwd");
                        channel=o.getString("channel");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }

        if(!server.equals("")&&!port.equals("")){
            MQTT_URL=server+port;
        }

        final TextView textView=(TextView)findViewById(R.id.sent_info);
        textView.setMovementMethod(new ScrollingMovementMethod());

        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        final ArrayList<Sensor> sensors=new ArrayList<>(manager.getSensorList(Sensor.TYPE_ALL));
        final ArrayList<Sensor> selected=new ArrayList<>();
        final ArrayList<String> sensorNames=new ArrayList<>();

        telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED){
            id=telephonyManager.getDeviceId();
        }
        final String fid=id;

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
            Log.i("shunqi",names.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        final JSONObject final_names=names;
        final JSONObject object=new JSONObject();
        listener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                time_map.put(1, System.currentTimeMillis());
                if ((time_map.get(1) - time_map.get(0) > interval) && (bool==true) ){
                    bool = false;
                    for (int i = 0; i < ia.length; i++) {
                        if (event.sensor.equals(selected.get(i))) {
                            JSONArray nameArray = null;
                            try {
                                nameArray = (JSONArray) final_names.get(sensorNames.get(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                for (int x = 0; x < event.values.length; x++) {

                                    if (nameArray == null) {
                                        object.put(fid+"-"+event.sensor.getName() + "-unknown-key" + (x + 1), event.values[x]);
                                    } else {
                                        object.put(fid+"-"+sensorNames.get(i) + "-" + (String) nameArray.get(x), event.values[x]);
                                    }
                                }
                                Log.i("shunqi", object.toString());
                                textView.setText(object.toString());
                                time_map.put(0, System.currentTimeMillis());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            final String payload = object.toString();

                            //test
                            //Toast.makeText(getApplicationContext(),payload,Toast.LENGTH_LONG).show();

                            try {

                                setupMqtt(getApplicationContext());
                                connectMqtt();

                                mqttClient.setCallback(new MqttCallbackExtended() {
                                    @Override
                                    public void connectComplete(boolean reconnect, String serverURI) {
                                        Log.d(TAG, "Connected to: " + serverURI);

                                        MqttMessage message = new MqttMessage();
                                        message.setPayload(payload.getBytes());
                                        try {
                                            mqttClient.publish("v1/devices/me/telemetry", message);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                        }
                                        bool = true;

                                    }

                                    @Override
                                    public void connectionLost(Throwable cause) {
                                        Log.e(TAG, "The Connection was lost.", cause);
                                    }

                                    @Override
                                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                                        Log.d(TAG, "Incoming message: " + new String(message.getPayload()));
                                    }

                                    @Override
                                    public void deliveryComplete(IMqttDeliveryToken token) {
                                        Log.d(TAG, "Published telemetry data: " + payload);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        time_map.put(0,System.currentTimeMillis());
        time_map.put(1,new Long(1));
        for(Sensor s:selected){
            manager.registerListener(listener,s,1000000);
        }

        //No data

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
                try{
                    object.put(fid+"-"+"Latitude",location.getLatitude());
                    object.put(fid+"-"+"Longitude",location.getLongitude());
                }catch (JSONException e){
                    e.printStackTrace();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterListener(listener);
    }

    void setupMqtt(Context ctx) {
        mqttClient = new MqttAndroidClient(getBaseContext(), MQTT_URL, MqttClient.generateClientId());
        mqttOptions = new MqttConnectOptions();

        /**
         * SSL broker requires a certificate to authenticate their connection
         * Certificate can be found in resources folder /res/raw/
         */

        SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
        try {
            // socketFactoryOptions.withCaInputStream(ctx.getResources().openRawResource(R.raw.client));
            socketFactoryOptions.withCaInputStream(ctx.getResources().openRawResource(R.raw.mqttsvr));
            socketFactoryOptions.withClientP12InputStream(ctx.getResources().openRawResource(R.raw.mqttclient));
            socketFactoryOptions.withClientP12Password(CLIENT_KEYSTORE_PASSWORD);
            mqttOptions.setSocketFactory(new SocketFactory(socketFactoryOptions));
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    void connectMqtt() {
        try {

            final IMqttToken token = mqttClient.connect(mqttOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "connected, token:" + asyncActionToken.toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e(TAG, "not connected: " + asyncActionToken.toString(), exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    void mqttDisconnect() {
        try {
            IMqttToken disconToken = mqttClient.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {


                    Log.e(TAG, "couldnt disconnect", exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
