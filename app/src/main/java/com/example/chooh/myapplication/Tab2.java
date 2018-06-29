package com.example.chooh.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Tab2 extends Fragment {
    public Tab2(){}
    private Connection connection=null;
    EditText input = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public void setConnection(Connection con){
        connection=con;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.send_data, container, false);

        Button send = view.findViewById(R.id.sendMsg);
        send.setOnClickListener(listener);
        input = view.findViewById(R.id.input);

        return view;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject object=new JSONObject();
            String inputString = input.getText().toString();
            System.out.println("oi"+inputString);
            try {
                object.put("Message",inputString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            connection.send(object.toString());
        }
    };
}
