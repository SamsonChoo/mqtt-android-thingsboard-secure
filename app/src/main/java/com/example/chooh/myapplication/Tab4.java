package com.example.chooh.myapplication;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;

public class Tab4 extends Fragment{
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

        if(externalMemoryAvailable()){
            File path= Environment.getExternalStorageDirectory();
            StatFs stat=new StatFs(path.getPath());
            long blockSize=stat.getBlockSizeLong();
            long available=stat.getAvailableBlocksLong();
            long total=stat.getBlockCountLong();

            int used=(int)(100*(total-available)/total);
            Log.i("shunqi",used+"");
            Log.i("shunqi","log here");
            memory.setProgress(used);

            String text=Math.round(available*blockSize/Math.pow(1024,3))
                    +"GB / "
                    +Math.round(total*blockSize/Math.pow(1024,3))
                    +"GB";
            memoryText.setText(text);
        }

        return view;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }
}
