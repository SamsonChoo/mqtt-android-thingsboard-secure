package com.example.chooh.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String MQTT_URL = "ssl://tb.hpe-innovation.center:8883";
    // private static final String clientId = "android-tb-mqtt-1";
    private static final String CLIENT_KEYSTORE_PASSWORD = "P@ssw0rd";

    private MqttAndroidClient mqttClient;
    private MqttConnectOptions mqttOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final JSONObject object=new JSONObject();
        try {
            object.put("meow",6);
            object.put("woof",1.120411204812381231);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String payload = object.toString();
        try {

            setupMqtt(this);
            connectMqtt();

            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.d(TAG, "Connected to: " + serverURI);
                    pub(payload);

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
                    Log.d(TAG, "Published telemetry data: " + payload );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            
            setupMqtt(this);
            connectMqtt();

            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.d(TAG, "Connected to: " + serverURI);
                    pub(payload);

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
                    Log.d(TAG, "Published telemetry data: " + payload );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }}

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Disconnecting MQTT connection");
        mqttDisconnect();
    }

    public void pub(String payload){
        MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        try {
            mqttClient.publish("v1/devices/me/telemetry", message);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
