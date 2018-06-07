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
    private ArrayList<Sensor> selected=new ArrayList<>();
    private ArrayList<String> sensorNames=new ArrayList<>();
    private ArrayList<Sensor> sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList=manager.getSensorList(Sensor.TYPE_ALL);
        sensors=new ArrayList<>(sensorList);

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

        int[] ia=new int[length];
        int pointer=0;
        for(int i=0;i<ba.size();i++){
            if(ba.get(i)){
                ia[pointer++]=i;
            }
        }

//        for(int i:ia){
//            Sensor sensor=sensors.get(i);
//            selected.add(sensor);
//            sensorNames.add(sensorAdapter.sensorTypeToString(sensor.getType()));
//        }
//
//        JSONObject names=null;
//        try{
//            AssetManager assetManager=getAssets();
//            InputStream is=assetManager.open("Test.json");
//            String content="";
//            InputStreamReader ir=new InputStreamReader(is);
//            BufferedReader br=new BufferedReader(ir);
//            String st;
//            while ((st=br.readLine())!=null){
//                content+=st;
//            }
//            System.out.println(content);
//            names=new JSONObject(content);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        final JSONObject object=new JSONObject();
//        final JSONObject final_names=names;
//
//        for(int j=0;j<ia.length;j++){
//            final int i=j;
//            final int len=ia.length-1;
//            SensorEventListener listener=new SensorEventListener() {
//                @Override
//                public void onSensorChanged(SensorEvent event) {
//                    if(event.sensor.equals(selected.get(i))){
//                        JSONArray nameArray=null;
//                        try{
//                            nameArray=(JSONArray)final_names.get(sensorNames.get(i));
//                        }catch(JSONException e){
//                            e.printStackTrace();
//                        }
//                        try{
//                            for(int x=0;x<event.values.length;x++){
//                                if(nameArray==null){
//                                    object.put(sensorNames.get(i)+"-unknown-key"+(x+1),event.values[x]);
//                                }else{
//                                    object.put(sensorNames.get(i)+"-"+(String)nameArray.get(x),event.values[x]);
//                                }
//                            }
//
//                            Log.i("shunqi",object.toString());
//                        }catch (JSONException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                @Override
//                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
//            };
//
//            manager.registerListener(listener,selected.get(i),3333333);
//        }


        Intent intent=new Intent(this, MQTTConnection.class);
        intent.putExtra("select",ia);
        startActivity(intent);
    }
}
