package com.example.chooh.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Tab3 extends Fragment{
    private ProgressBar battery;
    private TextView batteryLevel;

    private BroadcastReceiver br=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            battery.setProgress(level);
            batteryLevel.setText(level+"%");
        }
    };

    public Tab3(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery, container, false);

        battery=(ProgressBar)view.findViewById(R.id.battery);
        batteryLevel=(TextView)view.findViewById(R.id.level);

        getContext().registerReceiver(br,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        return view;
    }
}
