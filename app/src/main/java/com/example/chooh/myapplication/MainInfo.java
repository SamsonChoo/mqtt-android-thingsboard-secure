package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainInfo extends AppCompatActivity {
    private ViewPager mViewPager;

    private String configName="";
    private static String TAG = Info.class.getName();
    private static String serverUri = "ssl://tb.hpe-innovation.center:8883";
    private static String server="";
    private static String port="";
    private static String certFile = "client.bks";
    private static String certPwd = "P@ssw0rd";
    private static String channel;
    private static String clientId = "MQTT_SSL_ANDROID_CLIENT_BKS";
    private MqttAndroidClient mqttAndroidClient;
    private Uri uri=null;
    private Connection connection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);

        configName=getIntent().getStringExtra("configName");

        //Open  internal file config to choose the configuration user selected
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
                        uri= Uri.parse(o.getString("uri"));
                        certFile=o.getString("fileName");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }
        if(!server.equals("")&&!port.equals("")){
            serverUri ="ssl://" + server+ ":" + port;
        }

        connection=new Connection(serverUri,clientId,certFile,certPwd,uri,getApplicationContext());
        connection.setup();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        Sensors sensors=new Sensors();
        sensors.setConfigName(configName);
        adapter.addFrag(sensors, "Sensors");

        Tab1 tab1 = new Tab1();
        tab1.setConnection(connection);
        adapter.addFrag(tab1, "Phone Info");
        adapter.addFrag(new Tab2(), "Message");

        Tab3 tab3=new Tab3();
        tab3.setConnection(connection);
        adapter.addFrag(tab3, "Battery");

        Tab4 tab4=new Tab4();
        tab4.setConnection(connection);
        adapter.addFrag(tab4, "Storage");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void addFrag(Fragment fragment,String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }
    }
}
