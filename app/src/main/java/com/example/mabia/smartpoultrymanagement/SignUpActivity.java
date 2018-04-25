package com.example.mabia.smartpoultrymanagement;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

    String userName;
    String userPass;
    String confirmPass;
    EditText rName;
    EditText rPass;
    EditText rConfirmPass;
    Button btnSignUp;

    String TABLE_NAME="tbl_user";
    String DB_NAME="my_db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        createDatabase();

        btnSignUp=(Button)findViewById(R.id.btn_sing_up);

        rName= (EditText) findViewById(R.id.et_r_user);
        rPass = (EditText) findViewById(R.id.et_r_password);
        rConfirmPass = (EditText) findViewById(R.id.et_c_password);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = rName.getText().toString();
                userPass = rPass.getText().toString();
                confirmPass = rConfirmPass.getText().toString();

                if(!userPass.matches(confirmPass)){
                    Toast.makeText(SignUpActivity.this, "Password does not matched!!",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    //Log.e("////////////////Table","inserted");
                    if (userName.equals("") || userPass.equals("") || confirmPass.equals(""))
                    {
                        Toast.makeText(SignUpActivity.this, "Fields can not be empty",
                                Toast.LENGTH_LONG).show();
                    }
                        else {
                        insertQuery(userName,confirmPass);
                        Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }

                }
            }
        });


    }


    ////////////method for database------------------------
    public void createDatabase() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE,null);

        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (col_id INTEGER PRIMARY KEY, col_name VARCHAR, col_pass VARCHAR);";
        db.execSQL(createTableQuery);
        db.close();
       // Toast.makeText(getApplicationContext(), "Table Created", Toast.LENGTH_LONG).show();
    }

    public void insertQuery(String name, String pass)
    {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        String insertQuery="INSERT INTO "+TABLE_NAME+" (col_name,col_pass) VALUES ('"+name+"','"+pass+"');";
        db.execSQL(insertQuery);
        db.close();
        Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_LONG).show();
    }

}
