package com.example.mabia.smartpoultrymanagement;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by MABIA on 3/6/2018.
 */

public class DateSelection extends DialogFragment {

    private TextView dateTextView;
    Button dateButton;
    DatePicker datePicker;
   static DateSelect lis;

    public static DateSelection newInstance(DateSelect viewReload) {
        DateSelection fragment = new DateSelection();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        lis=viewReload;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_control, container, false);
         datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        dateButton = (Button) view.findViewById(R.id.date_button);

        setUp();
        return view;
    }
    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }
    public  void setUp()
    {

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();
                //Toast.makeText(ControlActivity.this, "day "+ day+" month "+month+" year "+year, Toast.LENGTH_LONG).show();
                JSONObject json = new JSONObject();
                String topicStatus = "turkey/date";
                try {
                    json.put("Day",day);
                    json.put("Month",month);
                    json.put("Year",year);

                    MqttMessage message = new MqttMessage();
                    message.setPayload(json.toString().getBytes());
                    Log.e("Error","dont know "+message);

                    lis.setDate(json);

                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        });
    }



    interface  DateSelect
    {
        void setDate(JSONObject json);
    }


}
