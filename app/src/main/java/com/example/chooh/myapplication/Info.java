package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;

import android.text.method.ScrollingMovementMethod;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Info extends AppCompatActivity {
    private static final int REQUEST_LOCATION=1;
    long LOCATION_REFRESH_TIME=333;
    float LOCATION_REFRESH_DISTANCE=1;

    private SensorEventListener listener;
    private SensorManager manager;
    private final Map<Integer,Long> time_map=new HashMap<>();

    private final int interval=200;


    private static String configName;
    private static String TAG = Info.class.getName();
    private static String serverUri = "ssl://tb.hpe-innovation.center:8883";
    private static String server="";
    private static String port="";
    private static String certFile = "client.bks";
    private static String certPwd = "P@ssw0rd";
    private static String channel;
    private static String clientId = "MQTT_SSL_ANDROID_CLIENT_BKS";
    private MqttAndroidClient mqttAndroidClient;
    private Connection connection;

    private String id;
    private TelephonyManager telephonyManager;
    private Uri uri=null;

    boolean bool = true;

    private static final int CAMERA_REQUEST=5;
    private boolean flashLightStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
        ActivityCompat.requestPermissions(Info.this,
                new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);

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
                        certPwd =o.getString("pwd");
                        channel=o.getString("channel");
                        uri=Uri.parse(o.getString("uri"));
                        certFile=o.getString("fileName");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }
        Log.i("shunqi",uri.toString());

        if(!server.equals("")&&!port.equals("")){
            serverUri ="ssl://" + server+ ":" + port;
        }

        connection=new Connection(serverUri,clientId,certFile,certPwd,uri,getApplicationContext());
        connection.setup();

        try{
            mqttAndroidClient = new MqttAndroidClient(this, serverUri, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(getSSLSocketFactory(this, certFile, certPwd));

            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //we are connected! subscribe for rpc
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "Failure " + exception.toString());

                }
            });
        }catch (MqttException e){
            e.printStackTrace();
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
        //final String fid = id
        final String fid="";

        for(int i:ia){
            Sensor sensor=sensors.get(i);
            selected.add(sensor);
            sensorNames.add(sensorAdapter.sensorTypeToString(sensor.getType()));
        }

        JSONObject names=null;

//        try{
//            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
//
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setSocketFactory(getSSLSocketFactory(getApplicationContext(), certFile, certPwd));
//
//            IMqttToken token = mqttAndroidClient.connect(options);
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    //we are connected! subscribe for rpc
//                    subscribeToTopic();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    // Something went wrong e.g. connection timeout or firewall problems
//                    Log.d(TAG, "Failure " + exception.toString());
//
//                }
//            });
//        }catch (MqttException e){
//            e.printStackTrace();
//        }

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
                    //bool = false;
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
                                        object.put(fid+event.sensor.getName() + "-unknown-key" + (x + 1), event.values[x]);
                                    } else {
                                        object.put(fid+sensorNames.get(i) + "-" + (String) nameArray.get(x), event.values[x]);
                                    }
                                }
                                //Log.i("shunqi", object.toString());
                                textView.setText(object.toString());
                                time_map.put(0, System.currentTimeMillis());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            final String payload = object.toString();

                            connection.send(payload);

//                            try {
//                                MqttConnectOptions options = new MqttConnectOptions();
//                                options.setSocketFactory(getSSLSocketFactory(getApplicationContext(), certFile, certPwd));
//
//                                IMqttToken token = mqttAndroidClient.connect(options);
//                                token.setActionCallback(new IMqttActionListener() {
//                                    @Override
//                                    public void onSuccess(IMqttToken asyncActionToken) {
//                                        // We are connected
//                                        Log.d(TAG, "onSuccess");
//                                        try {
//                                            String topic = "v1/devices/me/telemetry";
//                                            byte[] encodedPayload = new byte[0];
//                                            encodedPayload = payload.getBytes("UTF-8");
//                                            MqttMessage message = new MqttMessage(encodedPayload);
//                                            mqttAndroidClient.publish(topic, message);
//                                            bool = true;
//                                        } catch (MqttException | UnsupportedEncodingException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                                        // Something went wrong e.g. connection timeout or firewall problems
//                                        Log.d(TAG, "Failure " + exception.toString());
//
//                                    }
//                                });
//
//                            } catch (MqttException e) {
//                                e.printStackTrace();
//                            }
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
                    object.put(fid+"Latitude",location.getLatitude());
                    object.put(fid+"Longitude",location.getLongitude());
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
        //mqttDisconnect();
        connection.mqttDisconnect();
        manager.unregisterListener(listener);
    }


    private SSLSocketFactory getSSLSocketFactory(Context context, String keystore, String password) throws
            MqttSecurityException {
        try {
            InputStream keyStore = getContentResolver().openInputStream(uri);
            System.out.println("asdf"+keyStore);
            //InputStream keyStore =context.getResources().getAssets().open(keystore);
            KeyStore km = KeyStore.getInstance("BKS");
            km.load(keyStore, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(km, password.toCharArray());

            InputStream trustStore = getContentResolver().openInputStream(uri);
            System.out.println("asdf"+trustStore);
            KeyStore ts = KeyStore.getInstance("BKS");
            ts.load(trustStore, password.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ts);

            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return ctx.getSocketFactory();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException e) {
            throw new MqttSecurityException(e);
        }
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe("v1/devices/me/rpc/request/+", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG,"Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG,"Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe("v1/devices/me/rpc/request/+", 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                    String[] parts = topic.split("/");
                    String requestId = parts[5];
                    try {
                        if (flashLightStatus)
                            flashLightOff();
                        else
                            flashLightOn();
                        TextView message_received = (TextView)findViewById(R.id.message_received);
                        message_received.setText(new String(message.getPayload()));
                        String msg = "{\"meow\":\"meow\"}";
                        topic = "v1/devices/me/rpc/response/" + requestId;
                        byte[] encodedPayload = new byte[0];
                        encodedPayload = msg.getBytes("UTF-8");
                        message = new MqttMessage(encodedPayload);
                        mqttAndroidClient.publish(topic, message);
                        subscribeToTopic();
                    } catch (MqttException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    void mqttDisconnect() {
        try {
            IMqttToken disconToken = mqttAndroidClient.disconnect();
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

    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException e) {
        }
    }

    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException e) {
        }
    }
}
