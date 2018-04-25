package com.example.mabia.smartpoultrymanagement;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class ModeSettingActivity extends AppCompatActivity {

    MqttHelper mqttHelper;
    private RadioGroup radioGroupSetting;
    private RadioButton btnRadio;
    Button btnMode;

    Dialog modePopup;
    boolean isSelected_L1,isSelected_L2,isSelected_L3,isSelected_L4 = false;
    JSONObject json_light_status = new JSONObject();
    JSONObject jsnMode =  new JSONObject();

    byte lights_Status = 00000000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_setting);
        startMqtt();

        btnMode = (Button) findViewById(R.id.mode_button);
        radioGroupSetting = (RadioGroup) findViewById(R.id.radioGroup);
        btnMode.setEnabled(false);

        modePopup = new Dialog(this);
        modePopup.setCanceledOnTouchOutside(false);
        radioGroupSetting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int selectedId = radioGroupSetting.getCheckedRadioButtonId();
                btnRadio = (RadioButton) findViewById(selectedId);
                //Toast.makeText(ModeSettingActivity.this, btnRadio.getText(), Toast.LENGTH_SHORT).show();
                if(btnRadio.getText().equals("Manual")){
                    showPopup();
                    btnMode.setEnabled(false);
                    btnRadio.setChecked(false);
                }else {
                    btnMode.setEnabled(true);
                }
            }
        });


        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(ModeSettingActivity.this, (btnRadio.getText().toString()), Toast.LENGTH_SHORT).show();
                try {
                    Log.d("mode test","jdgk"+btnRadio.getText().toString().equals("Auto"));
                    if(btnRadio.getText().toString().equals("Auto")){
                        jsnMode.put("Mode","1");
                    }else {
                        jsnMode.put("Mode","0");
                    }
                    MqttMessage msgMode = new MqttMessage();
                    msgMode.setPayload(jsnMode.toString().getBytes());
                    mqttHelper.mqttAndroidClient.publish("turkey/mode", msgMode);

                    Intent dashboard = new Intent(ModeSettingActivity.this,DashboardActivity.class);
                    startActivity(dashboard);

                } catch (JSONException | MqttException e) {
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
                Toast.makeText(ModeSettingActivity.this, "Connection established ".toString(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(ModeSettingActivity.this, "Connection lost ".toString(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String top=new String(topic);
                Log.w("Debug", top);
                Log.d("Tag","**********Data: "+mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    public void showPopup(){
        TextView txtForClose;

        modePopup.setContentView(R.layout.custom_popup);
        txtForClose = (TextView) modePopup.findViewById(R.id.txtClose);

        txtForClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modePopup.dismiss();
                lights_Status = 00000000;
            }
        });
        modePopup.show();
    }

    public void lightSelection(View v){
        switch (v.getId()) {
            case R.id.lightTxt1:
                if(!isSelected_L1){
                    ((TextView)v).setTextColor(Color.RED); //black;
                    isSelected_L1 = true;
                    lights_Status |= (1<<0);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked on " + lights_Status)), Toast.LENGTH_SHORT).show();
                }else{
                    ((TextView)v).setTextColor(Color.WHITE);
                    isSelected_L1 = false;
                    lights_Status &=~(1<<0);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked off " + lights_Status)), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.lightTxt2:
                if(!isSelected_L2){
                    ((TextView)v).setTextColor(Color.RED); //black
                    isSelected_L2 = true;
                    lights_Status |= (1<<1);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked on " + lights_Status)), Toast.LENGTH_SHORT).show();
                }else{
                    ((TextView)v).setTextColor(Color.WHITE);
                    isSelected_L2 = false;
                    lights_Status &=~ (1<<1);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked off " + lights_Status)), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.lightTxt3:
                if(!isSelected_L3){
                    ((TextView)v).setTextColor(Color.RED); //black
                    isSelected_L3 = true;
                    lights_Status |= (1<<2);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked on " + lights_Status)), Toast.LENGTH_SHORT).show();
                }else{
                    ((TextView)v).setTextColor(Color.WHITE);
                    isSelected_L3 = false;
                    lights_Status &=~ (1<<2);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked off " + lights_Status)), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.lightTxt4:
                if(!isSelected_L4){
                    ((TextView)v).setTextColor(Color.RED); //black
                    isSelected_L4 = true;
                    lights_Status |= (1<<3);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked on " + lights_Status)), Toast.LENGTH_SHORT).show();
                }else{
                    ((TextView)v).setTextColor(Color.WHITE);
                    isSelected_L4 = false;
                    lights_Status &=~ (1<<3);
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked off " + lights_Status)), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.button_submit_LS:
                try {
                    jsnMode.put("Mode","0");
                    json_light_status.put("LightLevel",lights_Status);

                    MqttMessage msgMode = new MqttMessage();
                    msgMode.setPayload(jsnMode.toString().getBytes());
                    mqttHelper.mqttAndroidClient.publish("turkey/mode", msgMode);

                    MqttMessage msgLight = new MqttMessage();
                    msgLight.setPayload(json_light_status.toString().getBytes());
                    mqttHelper.mqttAndroidClient.publish("turkey/light", msgLight);
                    Log.d("final clicked ", String.valueOf(lights_Status));
                    //Toast.makeText(ModeSettingActivity.this, ( ("clicked " + lights_Status)), Toast.LENGTH_SHORT).show();
                } catch (JSONException | MqttException e) {
                    e.printStackTrace();
                }
                modePopup.dismiss();
                lights_Status = 00000000;
                btnMode.setEnabled(true);
                Intent dashboard = new Intent(ModeSettingActivity.this,DashboardActivity.class);
                startActivity(dashboard);
                break;
            default:
                break;
        }
    }
}
