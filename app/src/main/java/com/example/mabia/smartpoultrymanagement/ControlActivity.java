package com.example.mabia.smartpoultrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

//import com.example.mabia.smartpoultrymanagement.materialdatetimepicker.date.DatePickerDialog;

public class ControlActivity extends AppCompatActivity {

    MqttHelper mqttHelper;
    private TextView dateTextView;
    Button dateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        startMqtt();
        dateButton = (Button) findViewById(R.id.date_button);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
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

                    mqttHelper.mqttAndroidClient.publish(topicStatus, message);

                    Intent launchNewIntent = new Intent(ControlActivity.this,DashboardActivity.class);
                    startActivity(launchNewIntent);

                } catch (JSONException  | MqttException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(ControlActivity.this, "Connection established ".toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(ControlActivity.this, "Connection lost ".toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String top=new String(topic);
                Log.w("Debug", mqttMessage.toString());
                Log.d("Tag","**********Data: "+mqttMessage.toString());
                //Toast.makeText(ControlActivity.this, "**********Data: "+mqttMessage.toString(), Toast.LENGTH_LONG).show();
                //dataReceived.setText(mqttMessage.toString());
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
