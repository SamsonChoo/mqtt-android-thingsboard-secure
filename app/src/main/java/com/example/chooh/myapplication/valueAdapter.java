package com.example.chooh.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class valueAdapter extends BaseAdapter {
    Sensor sensor;
    SensorManager manager;
    Context context;
    int count=0;

    public valueAdapter(Context context,Sensor sensor,SensorManager manager){
        this.context=context;
        this.sensor=sensor;
        this.manager=manager;
    }
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=convertView;
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.one_value,null);
        }



        return view;
    }
}
