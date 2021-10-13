package com.keneth.android.androidnatural;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CuentaActivity extends AppCompatActivity {

    TextView logout1,contraseña;

    Button edit_profile;
    FirebaseUser firebaseUser;
    String profileid;

    //xd
    int vrible;
    //xd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Cuenta");//antes "Settings"
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //xd
        logout1 = findViewById(R.id.logout1);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getBaseContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none"); //no me cambia

        logout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(CuentaActivity.this, EditarPerfilActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        });


        /*contraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(CuentaActivity.this, AjustesActivity2.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


            }
        });*/
        //xd


    }


}