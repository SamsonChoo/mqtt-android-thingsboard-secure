package com.example.chooh.myapplication;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;

public class Tab4 extends Fragment{
    private String text="";
    private Connection connection;

    public Tab4(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memory, container, false);

        ProgressBar memory=(ProgressBar)view.findViewById(R.id.memory);
        TextView memoryText=(TextView)view.findViewById(R.id.memoryText);
        Button send=(Button)view.findViewById(R.id.sendMemory);

        if(externalMemoryAvailable()){
            File path= Environment.getExternalStorageDirectory();
            StatFs stat=new StatFs(path.getPath());

            File paht2=Environment.getDataDirectory();
            StatFs stat2=new StatFs(paht2.getPath());

            long blockSize=stat.getBlockSizeLong();
            long available=stat.getAvailableBlocksLong();
            long total=stat.getBlockCountLong();

            int used=(int)(100*(total-available)/total);
            memory.setProgress(used);

            text=Math.round((total-available)*blockSize/Math.pow(1024,3))
                    +"GB / "
                    +Math.round(total*blockSize/Math.pow(1024,3))
                    +"GB";
            memoryText.setText(text);
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(text!=""){
                    JSONObject object=new JSONObject();
                    try{
                        object.put("storage",text);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    connection.send(object.toString());
                }
            }
        });

        return view;
    }

    public void setConnection(Connection con){
        connection=con;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }
}
