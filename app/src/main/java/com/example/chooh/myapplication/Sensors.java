package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Sensors extends Fragment{

    private SensorManager manager;
    private sensorAdapter adapoer;
    private String configName="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);
        final Context context=getContext();

        manager=(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList=manager.getSensorList(Sensor.TYPE_ALL);

        final ListView sensors=(ListView)view.findViewById(R.id.sensor_list);
        adapoer=new sensorAdapter(context,sensorList);
        sensors.setAdapter(adapoer);

        Button Send=(Button)view.findViewById(R.id.send);
        Send.setOnClickListener(listener);

        Button Clear=(Button)view.findViewById(R.id.clear);
        Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapoer.reset();
                ///////////recreate();
                getActivity().recreate();
            }
        });

        return view;
    }

    public void setConfigName(String name){
        configName=name;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context=getContext();
            ArrayList<Boolean> ba = adapoer.getBa();
            int length = 0;
            for (boolean b : ba) {
                if (b) length++;
            }


            if(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context,"Please enable the PHONE permission!",Toast.LENGTH_LONG).show();
                return;
            }

            if(length==0){
                Toast.makeText(context,"Please select one sensor!",Toast.LENGTH_LONG).show();
                return;
            }

            int[] ia = new int[length];
            int pointer = 0;
            for (int i = 0; i < ba.size(); i++) {
                if (ba.get(i)) {
                    ia[pointer++] = i;
                }
            }

            Intent intent=new Intent(context, Info.class);
            intent.putExtra("select",ia);
            intent.putExtra("configName",configName);
            startActivity(intent);
        }
    };
}
