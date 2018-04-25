package com.example.mabia.smartpoultrymanagement;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.androidhive.sessions.SessionManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity implements  DateSelection.DateSelect{

    MqttHelper mqttHelper;
    // Session Manager Class
    SessionManager session;
    String mqttTopic;
    String humNotify="Humidity Notification",fanNotify;
    float temp,hum,gas_co2,co2_threshold;
    int checkfanStatus=2;
    int week,day,time;
    TextView tvFortemp,tvForHum,tvForCo2,tvForNH3,tvNotifyForTemp,tvNotifyForHum,tvNotifyForCO2,tvNotifyForLight,tvNotifyForTime;
    private Menu dmenu;
    MenuItem item;
    DateSelection loginDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        // Session class instance
        session = new SessionManager(getApplicationContext());

        // Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        //HashMap<String, String> user = session.getUserDetails();

        // name
       // String name = user.get(SessionManager.KEY_NAME);
        //password
       // String password = user.get(SessionManager.KEY_PASSWORD);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean status =  activeNetworkInfo != null && activeNetworkInfo.isConnected();

        co2_threshold = (float) 1200.00;

        tvFortemp = (TextView)findViewById(R.id.textViewTemp);
        tvForHum = (TextView) findViewById(R.id.textViewHum);
        tvForCo2 = (TextView) findViewById(R.id.textView_CO2);
        tvForNH3 = (TextView) findViewById(R.id.textView_NH3);

        tvNotifyForTemp = (TextView) findViewById(R.id.textViewTempNotification);
        tvNotifyForHum = (TextView) findViewById(R.id.textViewHumNotification);
        tvNotifyForLight = (TextView) findViewById(R.id.textViewLightNotification);
        tvNotifyForTime = (TextView) findViewById(R.id.textViewTimeNotification);
        tvNotifyForCO2 = (TextView) findViewById(R.id.textViewCO2Notification);


        if(status){
            startMqtt();
        }else {
           // Toast.makeText(DashboardActivity.this, "Internet connection failed, Connect to Internet", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("ATTENTION!!");
            builder.setMessage("Internet connection failed, Connect to Internet");
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        this.dmenu = menu;
        Log.d("___","checkfanStatus "+checkfanStatus);
        if (checkfanStatus == 0) {
            menu.findItem(R.id.switch_fan).setTitle("Fan Turn On");
        }else if(checkfanStatus == 1){
            menu.findItem(R.id.switch_fan).setTitle("Fan Turn Off");
        }else{
            menu.findItem(R.id.switch_fan).setTitle("Checking Fan");
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_control:

                 loginDialog = DateSelection.newInstance(this);
                loginDialog.show(getSupportFragmentManager());
                //Intent intent = new Intent();
//                Intent launchNewIntent = new Intent(DashboardActivity.this,ControlActivity.class);
//                startActivityForResult(launchNewIntent, 0);
                return true;
            case R.id.logout:
                new AlertDialog.Builder(DashboardActivity.this)
                        .setTitle("ATTENTION!!")
                        .setMessage("Are You Sure To Log Out?")
                        .setPositiveButton("Log Out",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        session.logoutUser();
                                        Intent signout = new Intent(DashboardActivity.this,LoginActivity.class);
                                        startActivity(signout);
                                        DashboardActivity.this.finish();
                                    }

                                }).setNegativeButton("Later", null).show();
                return true;
            case R.id.mode:
                Intent modeSetting = new Intent(DashboardActivity.this,ModeSettingActivity.class);
                startActivity(modeSetting);
                return true;
            case R.id.reset:
                JSONObject jsnReset = new JSONObject();
                try {
                    jsnReset.put("Restart","1");
                    MqttMessage msgReset = new MqttMessage();
                    msgReset.setPayload(jsnReset.toString().getBytes());
                    mqttHelper.mqttAndroidClient.publish("turkey/restart", msgReset);
                   // publishedTopic="turkey/ClientReq";
                    JSONObject objToConnect = new JSONObject();
                    objToConnect.put("ClientReq",1);
                    MqttMessage msgConnect = new MqttMessage();
                    msgConnect.setPayload(objToConnect.toString().getBytes());
                    mqttHelper.mqttAndroidClient.publish("turkey/ClientReq", msgConnect);
                } catch (MqttException | JSONException e) {
                    e.printStackTrace();
                }
                return true;
            //case R.id.pick_action_provider:return true;
            case R.id.switch_fan:
                JSONObject jsnFanSwitch = new JSONObject();
                String fanItem = item.getTitle().toString();
                Log.d("___","check title "+fanItem);
                try {
                    MqttMessage msgfan = new MqttMessage();
                    //mqtt_topic_sub_FanStatus = "turkey/ComFanStatus";//{"FanStatus":FanStatus}
                    if (fanItem.equals("Fan Turn On")) {
                        jsnFanSwitch.put("FanStatus","1");
                        item.setTitle("Fan Turn Off");
                        Log.d("___","Fan Turn On "+fanItem);
                    } else if (fanItem.equals("Fan Turn Off")) {
                        jsnFanSwitch.put("FanStatus","0");
                        item.setTitle("Fan Turn On");
                        Log.d("___","Fan Turn Off "+fanItem);
                    }
                    msgfan.setPayload(jsnFanSwitch.toString().getBytes());
                    mqttHelper.mqttAndroidClient.publish("turkey/ComFanStatus", msgfan);
                } catch (MqttException | JSONException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

//        item = menu.findItem(R.id.pick_action_provider);
//        Drawable icon = getResources().getDrawable(R.drawable.power);
//        icon.setColorFilter(getResources().getColor(R.color.powerColor), PorterDuff.Mode.SRC_IN);
//        item.setIcon(icon);
        return super.onPrepareOptionsMenu(menu);
    }

    //onPrepareOptionsMenu

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(DashboardActivity.this, "Connection established ".toString(), Toast.LENGTH_LONG).show();
                Log.d("gfdg","connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(DashboardActivity.this, "Connection lost ".toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //Toast.makeText(DashboardActivity.this, "messageArrived", Toast.LENGTH_LONG).show();
                Log.w("Debug", mqttMessage.toString());
                Log.d("Tag","**********Data: "+mqttMessage.toString());
                mqttTopic=new String(topic);
                Log.d("Topic","---------top-----------"+mqttTopic);
                if(mqttTopic.equals("turkey/ElapsedTime")){
                    JSONObject jsonForDate =  new JSONObject(mqttMessage.toString());
                    Log.d("Topic Value","---------ElapsedTime-----------"+jsonForDate.getString("ElapsedTime"));
                    time = Integer.parseInt(jsonForDate.getString("ElapsedTime"));
                    week = (time/7);
                    day =  (time%7);
                    Log.d("Topic Value","---------ElapsedTime----------- processing");
                    tvNotifyForTime.setText("Time passes "+week+" week "+" "+day+" day");
                    Log.d("Topic Value","---------ElapsedTime----------- processed");
                }
                if(mqttTopic.equals("turkey/data")){
                    JSONObject jsobj =  new JSONObject(mqttMessage.toString());
//                    Log.d("Topic Value","---------temp-----------"+jsobj.getString("Temp")+" ---hum-------- "+
//                            jsobj.getString("Hum")+"---CO2-------- "+jsobj.getString("Co2")+"---NH3-------- "+jsobj.getString("NH3"));
                    tvFortemp.setText(jsobj.getString("Temp")+" °C");
                    tvForHum.setText(jsobj.getString("Hum")+" %");
                    tvForCo2.setText(jsobj.getString("Co2")+" ppm");
                    tvForNH3.setText(jsobj.getString("NH3")+" ppm");
                    // Humidity Checking
                    hum = Float.parseFloat(jsobj.getString("Hum"));
                    if(hum >= 40.00 && hum <= 60.00){humNotify = "Humidity is in correct range";}
                    else{humNotify = "Humidity is not in correct range";}
                    //CO2 checking
                    gas_co2 =Float.parseFloat(jsobj.getString("Co2"));
                    if (gas_co2 >= 0.00 && gas_co2 <= co2_threshold) {tvNotifyForCO2.setText("Co2 is in correct range");}
                    else{tvNotifyForCO2.setText("Co2 is not in correct range");}
                    temp = Float.parseFloat(jsobj.getString("Temp"));
                }
                if (mqttTopic.equals("turkey/FanStatus")) {
                    JSONObject jsnFan = new JSONObject(mqttMessage.toString());
                    Log.d("___","FanStatus "+jsnFan.getString("FanStatus"));

                    checkfanStatus = Integer.parseInt(jsnFan.getString("FanStatus"));
                    //MenuItem fanItem = (MenuItem)findViewById(R.id.switch_fan);
                    MenuItem fanItem = dmenu.findItem(R.id.switch_fan);
                    fanItem.setTitle((checkfanStatus == 0)?"Fan Turn On":"Fan Turn Off");

                    fanNotify = (checkfanStatus == 0)?"OFF" : "ON";
                    Log.d("___","FanStatusText "+fanNotify);
                    tvNotifyForHum.setText(humNotify+" and Fan is "+fanNotify);
                }
                if(mqttTopic.equals("turkey/LightStatus")) {
                    JSONObject jsobjForLight =  new JSONObject(mqttMessage.toString());
                    Log.d("Topic Value","---------LightStatus-----------"+jsobjForLight.getString("LightStatus"));
                    DisplayLightNotification(jsobjForLight.getString("LightStatus"),jsobjForLight.getString("Mode"));

                }

                //Change icon color when device is connected
                if (mqttTopic.equals("turkey/DeviceConnection")) {
                    JSONObject jsonDevice = new JSONObject(mqttMessage.toString());
                    //Drawable icon = getResources().getDrawable(R.drawable.power);
                    Log.d("___CheckConnection","Cc "+jsonDevice.getString("CheckConnection").equals("1"));
                    if (jsonDevice.getString("CheckConnection").equals("1")) {
                        //icon.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
                        //item.setIcon(icon);
                    }else{
                        //icon.setColorFilter(getResources().getColor(R.color.powerColor), PorterDuff.Mode.SRC_IN);
                        //item.setIcon(icon);
                    }
                }
                displayNotification(temp,time);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Toast.makeText(DashboardActivity.this, "Delivery Completed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayNotification(Float temperature,Integer elapsed_time){
        //Toast.makeText(DashboardActivity.this, "Delivery Completed "+temperature , Toast.LENGTH_SHORT).show();
        int week;
        //day;
        week = (elapsed_time/7);
        //day = (int) (elapsed_time%7);

        // Log.d("Check","Time "+elapsed_time);
        //tvNotifyForTime.setText("Time passes "+week+" week "+" "+day+" day");
        tvNotifyForTemp.setTextColor(Color.WHITE);
        if(week == 0 && (temperature >= 35.00 && temperature <= 37.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 1 && (temperature >= 32.00 && temperature <= 34.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 2 && (temperature >= 24.00 && temperature <= 31.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 3 && (temperature >= 27.00 && temperature <= 28.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else if(week == 4 && (temperature >= 24.00 && temperature <= 26.00)){
            tvNotifyForTemp.setText("Temperature is in correct range");
        }else {
            tvNotifyForTemp.setText("Temperature is not in range for current week");
            tvNotifyForTemp.setTextColor(Color.RED);
        }

    }

    public void DisplayLightNotification(String lightStatuses,String mode) {
        int l1,l2,l3,l4;
        String txtL1,txtL2,txtL3,txtL4;
        Log.d("Decimal","to Binary "+ mode.equals("0"));
        int number = Integer.parseInt(lightStatuses);

        l1 = number&(1<<0);
        l2 = number&(1<<1);
        l3 = number&(1<<2);
        l4 = number&(1<<3);
        Log.d("shift Light1", String.valueOf(l1)+" Light2 "+ String.valueOf(l2)+" Light3 " + String.valueOf(l3) + " Light4 "+ String.valueOf(l4));
        txtL1 = (l1 == 0)? "OFF":"ON";
        txtL2 = (l2 == 0)? "OFF":"ON";
        txtL3 = (l3 == 0)? "OFF":"ON";
        txtL4 = (l4 == 0)? "OFF":"ON";


        SpannableStringBuilder builder = new SpannableStringBuilder();
        if(mode.equals("1")) builder.append("AUTO : ");
        else builder.append("MANUAL : ");

        String red = "L1 is "+txtL1 +" , ";
        SpannableString redSpannable= new SpannableString(red);
        if (l1==0) {
            redSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, red.length(), 0);
        }else{
            redSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, red.length(), 0);
        }
        builder.append(redSpannable);

        String white = "L2 is "+txtL2 + " , ";
        SpannableString whiteSpannable= new SpannableString(white);
        if (l2==0) {
            whiteSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, white.length(), 0);
        }else{
            whiteSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, white.length(), 0);
        }
        builder.append(whiteSpannable);

        String blue = "L3 is "+txtL3 + " , ";
        SpannableString blueSpannable = new SpannableString(blue);
        if (l3==0) {
            blueSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, blue.length(), 0);
        }else{
            blueSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, blue.length(), 0);
        }
        builder.append(blueSpannable);

        String green = "L4 is "+txtL4;
        SpannableString greenSpannable = new SpannableString(green);
        if (l4==0) {
            greenSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, green.length(), 0);
        }else{
            greenSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, green.length(), 0);
        }
        builder.append(greenSpannable);

        tvNotifyForLight.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public void onBackPressed() {
        // do nothing.
        new AlertDialog.Builder(DashboardActivity.this)
                .setTitle("ATTENTION!!")
                .setMessage("Are You Sure To Log Out?")
                .setPositiveButton("Log Out",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                session.logoutUser();
                                Intent intent= new Intent(DashboardActivity.this, LoginActivity.class);
                                startActivity(intent);
                                DashboardActivity.this.finish();
                            }

                        }).setNegativeButton("Later", null).show();

    }

    @Override
    public void setDate(JSONObject json) {
        if(json!=null)
        {
            String topicStatus = "turkey/date";
            MqttMessage message = new MqttMessage();
            message.setPayload(json.toString().getBytes());
            try {
                mqttHelper.mqttAndroidClient.publish(topicStatus, message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        loginDialog.dismiss();
    }
}
