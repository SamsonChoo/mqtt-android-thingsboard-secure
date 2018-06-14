package com.example.chooh.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration extends AppCompatActivity {
    private final String configs="config";
    private EditText name;
    private EditText server;
    private EditText port;
    private EditText keyPwd;
    private JSONArray array=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        final RelativeLayout first_part=(RelativeLayout)findViewById(R.id.first_part);
        final RelativeLayout second_part=(RelativeLayout)findViewById(R.id.second_part);
        second_part.setVisibility(View.GONE);

        TextView firstTime=(TextView)findViewById(R.id.firstTime);
        final Spinner configSpinner=(Spinner)findViewById(R.id.configs);
        final Button add=(Button)findViewById(R.id.addConfig);
        Button sel=(Button)findViewById(R.id.select);
        Button del=(Button)findViewById(R.id.del);

        name=(EditText)findViewById(R.id.inputName);
        final Spinner channel=(Spinner)findViewById(R.id.channels);
        server=(EditText)findViewById(R.id.inputServer);
        port=(EditText)findViewById(R.id.inputPort);
        keyPwd=(EditText)findViewById(R.id.inputPwd);
        Button save=(Button)findViewById(R.id.save);
        Button cancel=(Button)findViewById(R.id.cancel);


        File dir=this.getFilesDir();
        final File file=new File(dir,configs);

        String content="";
        try{
            BufferedReader br=new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine())!=null){
                content+=line;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.i("shunqi",content);
        if(content.equals("[]")||content.equals("")){
            array=new JSONArray();
            File fileNew=new File(this.getFilesDir(),configs);
        }else{
            firstTime.setVisibility(View.GONE);
            try{
                array=new JSONArray(content);
            }catch (JSONException e){
                e.printStackTrace();
            }

            List<String> configList=new ArrayList<>();
            for(int i=0;i<array.length();i++){
                try{
                    JSONObject o=array.getJSONObject(i);
                    configList.add(o.getString("name"));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item,configList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            configSpinner.setAdapter(adapter);
        }

        if(configSpinner.getSelectedItem()!=null){
            if(!configSpinner.getSelectedItem().toString().equals("")){
                sel.setVisibility(View.VISIBLE);
                del.setVisibility(View.VISIBLE);
            }
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_part.setVisibility(View.GONE);
                second_part.setVisibility(View.VISIBLE);
                setEmpty();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean empty=checkEmpty();
                if(empty){
                    AlertDialog alertDialog = new AlertDialog.Builder(Configuration.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Please fill in all empty fields.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }else {
                    String nameSave=name.getText().toString();

                    if(array==null){
                        return;
                    }
                    else{
                        for(int i=0;i<array.length();i++){
                            try{
                                JSONObject o=array.getJSONObject(i);
                                if(nameSave.equals(o.getString("name"))){
                                    AlertDialog alertDialog = new AlertDialog.Builder(Configuration.this).create();
                                    alertDialog.setTitle("Alert");
                                    alertDialog.setMessage("Config name already in use.");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                    return;
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }

                    String serverSave=server.getText().toString();
                    String portSave=port.getText().toString();
                    String pwdSave=keyPwd.getText().toString();

                    int pos=channel.getSelectedItemPosition();
                    String channelSave;
                    if(pos==0){
                        channelSave="MQTT";
                    }else{
                        channelSave="HTTP";
                    }

                    JSONObject objectSave=new JSONObject();
                    try {
                        objectSave.put("name",nameSave);
                        objectSave.put("server",serverSave);
                        objectSave.put("port",portSave);
                        objectSave.put("pwd",pwdSave);
                        objectSave.put("channel",channelSave);
                        array.put(objectSave);

                        FileOutputStream outputStream;
                        try{
                            outputStream=openFileOutput(configs, Context.MODE_PRIVATE);
                            outputStream.write(array.toString().getBytes());
                            outputStream.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        recreate();
                    }catch (JSONException e){
                        AlertDialog alertDialog = new AlertDialog.Builder(Configuration.this).create();
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage("Exception!");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        e.printStackTrace();
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),Main.class);
                intent.putExtra("configName",configSpinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(Configuration.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Are you sure to delete this config?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Sure",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                List<JSONObject> objects=new ArrayList<>();
                                for(int i=0;i<array.length();i++){
                                    try{
                                        JSONObject o=array.getJSONObject(i);
                                        objects.add(o);
                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }

                                objects.remove(configSpinner.getSelectedItemPosition());
                                array=new JSONArray(objects);

                                FileOutputStream outputStream;
                                try{
                                    outputStream=openFileOutput(configs, Context.MODE_PRIVATE);
                                    outputStream.write(array.toString().getBytes());
                                    outputStream.close();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                recreate();
                            }
                        });
                alertDialog.show();

            }
        });
    }

    boolean checkEmpty(){
        if(name.getText().toString().equals("")||
                server.getText().toString().equals("")||
                port.getText().toString().equals("")||
                keyPwd.getText().toString().equals(""))return true;
        else return false;
    }

    void setEmpty(){
        name.setText("");
        server.setText("");
        port.setText("");
        keyPwd.setText("");
    }

    boolean first=true;
    long start;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(first){
                start=System.currentTimeMillis();
                first=false;
            }else {
                long now=System.currentTimeMillis();
                Log.i("shunqi",(now-start)+"");
                if(now-start<5000){
                    finish();
                    moveTaskToBack(true);
                    return super.onKeyDown(keyCode, event);
                }else {
                    start=System.currentTimeMillis();
                }
            }
        }
        Toast.makeText(this,"Press back button again to exit.",Toast.LENGTH_LONG).show();
        return true;
    }
}
