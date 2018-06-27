package com.example.chooh.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Tab3 extends Fragment{
    private ProgressBar battery;
    private TextView batteryLevel;
    int level=-1;
    private Connection connection=null;

    private BroadcastReceiver br=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            battery.setProgress(level);
            batteryLevel.setText(level+"%");
        }
    };

    public Tab3(){}

    public void setConnection(Connection con){
        connection=con;
    }

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
        Button send=(Button)view.findViewById(R.id.sendBattery);
        send.setOnClickListener(listener);

        getContext().registerReceiver(br,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        return view;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject object=null;
            if(level!=-1){
                object=new JSONObject();
                try{
                    object.put("battery",String.valueOf(level));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            if(object!=null){
                if(connection==null){
                    Toast.makeText(getContext(),"Connection is null!",Toast.LENGTH_LONG).show();
                    return;
                }
                Log.i("shunqi",String.valueOf(connection==null));
                connection.send(object.toString());
            }
        }
    };
}
