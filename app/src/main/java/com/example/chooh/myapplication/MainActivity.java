package com.example.chooh.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private static final String MQTT_URL = "ssl://thingsboard:8883";
    private static final String clientId = "MQTT_SSL_JAVA_CLIENT";
    private static final String CLIENT_KEYSTORE_PASSWORD = "P@ssw0rd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            InputStream in = this.getApplicationContext().getResources().openRawResource(R.raw.client);
            MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(),MQTT_URL,clientId);
            client.getSSLSocketFactory(in,CLIENT_KEYSTORE_PASSWORD);
            MqttMessage message = new MqttMessage();
            message.setPayload("{\"key1\":\"value1\", \"key2\":true, \"key3\": 3.0, \"key4\": 4}".getBytes());
            client.publish("v1/devices/me/telemetry", message);
            client.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
