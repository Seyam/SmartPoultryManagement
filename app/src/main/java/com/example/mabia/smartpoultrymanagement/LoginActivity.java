package com.example.mabia.smartpoultrymanagement;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidhive.sessions.SessionManager;

public class LoginActivity extends AppCompatActivity {

    TextView txtForSignUp;

    String userName;
    String userPass;
    String registeredUserName;
    String registeredUserPass;
    EditText etName;
    EditText etPass;
    Button btnSignIn;

    String TABLE_NAME="tbl_user";
    String DB_NAME="my_db";

    // Session Manager Class
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createDatabase();
        // Session Manager
        session = new SessionManager(getApplicationContext());

        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        if (session.isLoggedIn()) {
            Intent intent1 = new Intent(LoginActivity.this,DashboardActivity.class);
            startActivity(intent1);
        }

        btnSignIn = (Button)findViewById(R.id.button_login);
        etName = (EditText)findViewById(R.id.editNameText);
        etPass=(EditText)findViewById(R.id.editPassText);

        txtForSignUp = (TextView) findViewById(R.id.tv_sign_up);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              String userName=  etName.getText().toString();
                String userPass=  etPass.getText().toString();

//                if(etName.getText().toString().equals("a") && etPass.getText().toString().equals("a")){
//                    //Toast.makeText(LoginActivity.this, "Successfully log in", Toast.LENGTH_LONG).show();
//                       Intent intent1 = new Intent(LoginActivity.this,DashboardActivity.class);
//                    startActivity(intent1);
//                }else{
//                    Toast.makeText(LoginActivity.this, "Can not log in", Toast.LENGTH_LONG).show();
//               }
                if (userName.trim().length() == 0 || userPass.trim().length() == 0)
                {
                    Toast.makeText(LoginActivity.this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    search(userName,userPass);
                }

            }
        });

        txtForSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        SignUpActivity.class);
                startActivity(intent);
            }
        });

    }


    ////search for sign in-----------------
    public void search(String sName,String sPass) {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Cursor cursor=db.rawQuery("SELECT * FROM "+TABLE_NAME+" order by col_id desc;", null);

        int rowCount=cursor.getCount();
        int count = cursor.getCount();
        if(rowCount<=0)
        {
            Toast.makeText(getApplicationContext(), "No data available please sign up first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            int flag=0;
            cursor.moveToFirst();
            for(int i = 0;i<rowCount;i++) {
                String name=cursor.getString(cursor.getColumnIndex("col_name"));
                String pass=cursor.getString(cursor.getColumnIndex("col_pass"));
                if (name.equals(sName) && pass.equals(sPass)) {
                    flag = 1;
                    break;
                }else{
                    flag = 0;
                }
            }

            if (flag == 1) {
                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                // Creating user login session
                // For testing i am stroing name, email as follow
                // Use user real data
                session.createLoginSession(sName, "anroidhive@gmail.com",sPass);

                // Staring MainActivity
                Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(i);
                finish();
//                Intent intent1 = new Intent(LoginActivity.this,DashboardActivity.class);
//                startActivity(intent1);
            }else{
                Toast.makeText(getApplicationContext(), "Wrong name or pass", Toast.LENGTH_SHORT).show();
            }
        }
        db.close();


    }
    public void createDatabase() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE,null);

        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (col_id INTEGER PRIMARY KEY, col_name VARCHAR, col_pass VARCHAR);";
        db.execSQL(createTableQuery);
        db.close();
        // Toast.makeText(getApplicationContext(), "Table Created", Toast.LENGTH_LONG).show();
    }
}
