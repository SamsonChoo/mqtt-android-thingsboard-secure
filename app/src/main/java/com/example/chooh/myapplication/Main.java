package com.example.chooh.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity implements View.OnClickListener{

    private SensorManager manager;
    private sensorAdapter adapoer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList=manager.getSensorList(Sensor.TYPE_ALL);

        ListView sensors=(ListView)findViewById(R.id.sensor_list);
        adapoer=new sensorAdapter(this,sensorList);
        sensors.setAdapter(adapoer);

        Button Send=(Button)findViewById(R.id.send);
        Send.setOnClickListener(this);

        Button Clear=(Button)findViewById(R.id.clear);
        Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapoer.reset();
                recreate();
            }
        });
    }

    @Override
    public void onClick(View v) {
        ArrayList<Boolean> ba=adapoer.getBa();
        int length=0;
        for(boolean b:ba){
            if(b)length++;
        }

        if(length==0){
            Toast.makeText(this,"Please select one sensor!",Toast.LENGTH_LONG).show();
            return;
        }

        int[] toSend=new int[length];
        int pointer=0;
        for(int i=0;i<ba.size();i++){
            if(ba.get(i)){
                toSend[pointer++]=i;
            }
        }

        Intent intent=new Intent(this, Info.class);
        intent.putExtra("select",toSend);
        startActivity(intent);
    }
}
