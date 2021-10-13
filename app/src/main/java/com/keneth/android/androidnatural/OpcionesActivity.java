package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OpcionesActivity extends AppCompatActivity {

    TextView logout,logout1,cuenta,Notificaciones,contraseña, settings;

    ImageView close;

    Button edit_profile;
    FirebaseUser firebaseUser;
    String profileid;

    //xd
    int vrible;
    //xd

    //contraseña
    FirebaseAuth firebaseAuth;
    //contraseña

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Configuración");//antes "Settings"
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);*/

        logout = findViewById(R.id.logout);

        //xd
        close = findViewById(R.id.close);
        logout1 = findViewById(R.id.logout1);
        //cuenta = findViewById(R.id.cuenta);
        Notificaciones = findViewById(R.id.Notificaciones);
        contraseña = findViewById(R.id.contraseña);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getBaseContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none"); //no me cambia

        //contraseña
        firebaseAuth = FirebaseAuth.getInstance();
        //contraseña

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpcionesActivity.super.onBackPressed();
            }
        });

        logout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(OpcionesActivity.this, EditarPerfilActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        });

        /*cuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(OpcionesActivity.this, CuentaActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        });*/

        Notificaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(OpcionesActivity.this, AjustesActivity2.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


            }
        });

        contraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showChangePasswordDialog();
            }
        });
        //xd

        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Opciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        logout.setOnClickListener(new View.OnClickListener() {         //funcion para CERRAR SESION
            @Override
            public void onClick(View v) {

                //xd2
                showDialog2();

                //xd2



            }
        });
    }

    //xd2
    private void FuncionSalir(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(OpcionesActivity.this, SplashScreenActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

    }


    private void showDialog2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(OpcionesActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogosalir, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button btnSi = view.findViewById(R.id.btnSi);
        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FuncionSalir();

                Toast.makeText(getBaseContext(), "SI", Toast.LENGTH_SHORT).show();
                vrible=2;
                dialog.dismiss();
            }
        });

        Button btnNo = view.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getBaseContext(), "NO", Toast.LENGTH_SHORT).show();
                vrible=1;
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    //xd2

    //CONTRASEÑA
    private void showChangePasswordDialog() {
        //diálogo de cambio de contraseña con diseño personalizado que tiene currentPassword, newPassword y botón de actualización

        //inflar el diseño para el diálogo
        View view = LayoutInflater.from(OpcionesActivity.this).inflate(R.layout.dialog_modificar_contrase, null);
        EditText passwordEt = view.findViewById(R.id.passwordEt);
        EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(OpcionesActivity.this);
        builder.setView(view); //establecer vista en diálogo

        android.app.AlertDialog dialog = builder.create();
        dialog.show();


        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validar datos
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(OpcionesActivity.this, "introduce tu contraseña actual...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(OpcionesActivity.this, "Contraseña menor a 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        //pd.show();

        //Obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //antes de cambiar la contraseña, vuelva a autenticar al usuario
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //autenticación con éxito, comenzar la actualización

                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Contraseña actualiza
                                        //pd.dismiss();
                                        Toast.makeText(OpcionesActivity.this, "Contraseña Actualizada...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //fallo la actualizacion de la contraseña, mostrar razon
                                        Toast.makeText(OpcionesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //fallo la autenticación, mostrar la razón
                        //pd.dismiss();
                        Toast.makeText(OpcionesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //CONTRASEÑA
}