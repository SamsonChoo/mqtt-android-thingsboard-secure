package com.example.chooh.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wsq96 on 2018/5/15.
 */

public class sensorAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Sensor> sensors;
    private ArrayList<Boolean> ba=new ArrayList<>();



    public sensorAdapter(Context context,List<Sensor> sensors){
        this.context=context;
        this.sensors=new ArrayList<>(sensors);
        for(int i=0;i<sensors.size();i++){
            ba.add(false);
        }
    }

    @Override
    public int getCount() {
        return sensors.size();
    }

    @Override
    public Object getItem(int i) {
        return sensors.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        View view=convertView;
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.one_sensor, null);
        }

        final Sensor sensor=sensors.get(i);

        TextView name=(TextView)view.findViewById(R.id.name);
        name.setText(sensor.getName());

        TextView type=(TextView)view.findViewById(R.id.type);
        final String sensor_type=sensorTypeToString(sensor.getType());
        type.setText(sensor_type);

        if(ba.get(i)){
            view.setBackgroundColor(Color.parseColor("#BAE1FC"));
        }else{
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        final View thisView=view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ba.get(i)){
                    ba.set(i,false);
                    thisView.setBackgroundColor(Color.TRANSPARENT);
                }else {
                    ba.set(i,true);
                    thisView.setBackgroundColor(Color.parseColor("#BAE1FC"));
                }
            }
        });

        return view;
    }

    public ArrayList<Boolean> getBa(){
        return new ArrayList<>(ba);
    }

    public void reset(){
        for(Boolean b:ba){
            b=false;
        }
    }

    public static String sensorTypeToString(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                return "Game Rotation Vector";
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return "Geomagnetic Rotation Vector";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "Gyroscope Uncalibrated";
            case Sensor.TYPE_HEART_RATE:
                return "Heart Rate Monitor";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "Magnetic Field Uncalibrated";
            case Sensor.TYPE_ORIENTATION:
                return "Orientation";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Relative Humidity";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                return "Significant Motion";
            case Sensor.TYPE_STEP_COUNTER:
                return "Step Counter";
            case Sensor.TYPE_STEP_DETECTOR:
                return "Step Detector";
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                return "Accelerometer Uncalibrated";
            case Sensor.TYPE_DEVICE_PRIVATE_BASE:
                return "Device Private Base";
            case Sensor.TYPE_HEART_BEAT:
                return "Heart Beat Monitor";
            case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT:
                return "Low Latency Offbody Detect";
            case Sensor.TYPE_MOTION_DETECT:
                return "Motion Detect";
            case Sensor.TYPE_POSE_6DOF:
                return "6-DoF Pose Detect";
            case Sensor.TYPE_STATIONARY_DETECT:
                return "Stationary Detect";
            default:
                return "Unknown";
        }
    }

    public ArrayList<Sensor> getSensors() {
        return new ArrayList<>(sensors);
    }
}
