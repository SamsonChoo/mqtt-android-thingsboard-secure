package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Tab1 extends Fragment{
    private TelephonyManager manager;

    public Tab1(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.phone_info,container,false);

        String id="";
        TelephonyManager manager=(TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(getContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED){
            id=manager.getDeviceId();
        }

        TextView manufacturer=(TextView)view.findViewById(R.id.manufacturer);
        TextView model=(TextView)view.findViewById(R.id.model);
        TextView sdk=(TextView)view.findViewById(R.id.sdk);
        TextView version=(TextView)view.findViewById(R.id.version);
        TextView imei=(TextView)view.findViewById(R.id.imei);
        TextView serial=(TextView)view.findViewById(R.id.serial);

        String manufacturerString= Build.MANUFACTURER;
        String modelString=Build.MODEL;
        String serialString=Build.SERIAL;

//        manufacturer.setText("sth");
//        model.setText(modelString);
//        sdk.setText(Build.VERSION.SDK_INT);
//        version.setText(Build.VERSION.RELEASE);
//        imei.setText(id);
//        serial.setText(serialString);

        return view;
    }
}
