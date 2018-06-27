package com.example.chooh.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Tab1 extends Fragment{
    private TelephonyManager manager;

    public Tab1(){}

    private Connection connection=null;

    private JSONObject object = new JSONObject();

    public void setConnection(Connection con){
        connection=con;
    }

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
        String sdkString=Build.VERSION.SDK_INT+"";
        String versionString=Build.VERSION.RELEASE+"";
        String serialString=Build.SERIAL;

        manufacturer.setText(manufacturerString);
        model.setText(modelString);
        sdk.setText(sdkString);
        version.setText(versionString);
        imei.setText(id);
        serial.setText(serialString);

        try {
            object.put("Manufacturer",manufacturerString);
            object.put("Model",modelString);
            object.put("Android SDK",sdkString);
            object.put("Android version",versionString);
            object.put("IMEI",id);
            object.put("serial",serialString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button send=(Button)view.findViewById(R.id.sendPhoneInfo);
        send.setOnClickListener(listener);

        return view;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            connection.send(object.toString());
        }
    };
}
