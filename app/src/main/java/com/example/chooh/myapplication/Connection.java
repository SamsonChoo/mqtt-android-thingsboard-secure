package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.google.common.io.Files;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Connection {
    private MqttAndroidClient mqttAndroidClient;
    private static String TAG = Connection.class.getName();
    private static String serverUri = "";
    private static String clientId = "MQTT_SSL_ANDROID_CLIENT_BKS";
    private static String certPwd = "";
    private static String configName="";
    private Context context=null;

    public Connection(String server,String id,String pwd,String configName,Context context){
        serverUri=server;
        clientId=id;
        certPwd=pwd;
        this.configName=configName;
        this.context=context;
    }

    public void setup(){
        try{
            mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(getSSLSocketFactory(context, certPwd));

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
    }

    public void send(final String toSend){
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(getSSLSocketFactory(context, certPwd));

            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    try {
                        String topic = "v1/devices/me/telemetry";
                        byte[] encodedPayload = new byte[0];
                        encodedPayload = toSend.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        mqttAndroidClient.publish(topic, message);
                    } catch (MqttException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    //Log.d(TAG, "Failure " + exception.toString());

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private SSLSocketFactory getSSLSocketFactory(Context context, String password) throws
            MqttSecurityException {
        try {
            FileInputStream keyStore=context.openFileInput(configName);

            System.out.println("asdf"+keyStore);
            KeyStore km = KeyStore.getInstance("BKS");
            km.load(keyStore, password.toCharArray());
            keyStore.close();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(km, password.toCharArray());

            FileInputStream trustStore=context.openFileInput(configName);
            System.out.println("asdf"+trustStore);
            KeyStore ts = KeyStore.getInstance("BKS");
            ts.load(trustStore, password.toCharArray());
            trustStore.close();
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
}
