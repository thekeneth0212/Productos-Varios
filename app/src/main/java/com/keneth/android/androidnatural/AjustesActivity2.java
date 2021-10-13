package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class AjustesActivity2 extends AppCompatActivity {

    //inicializamos vistas
    SwitchCompat postSwitch;

    //utilizar preferencias compartidas para guardar el estado del interruptor
    SharedPreferences sp;
    SharedPreferences.Editor editor; //para editar el valor de la preferencia de compartir

    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST"; //Asigne cualquier valor pero use el mismo para este tipo de notificaciones


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Ajustes");//antes "Settings"
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        postSwitch = findViewById(R.id.postSwitch);

        //init sp
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnable = sp.getBoolean(""+TOPIC_POST_NOTIFICATION,false);
        //si está habilitado, marque el interruptor; de lo contrario, desmarque el interruptor; de forma predeterminada, no está marcado / es falso
        if (isPostEnable){
            postSwitch.setChecked(true);
        }
        else {
            postSwitch.setChecked(false);
        }

        //implementar conmutador de cambio de escucha
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //editar estado del interruptor
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked){
                    subscribePostNotification(); //llamar para suscribirse
                }
                else {
                    unsubscribePostNotification(); //llamar para darse de baja

                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubscribePostNotification() {
        //darse de baja de un tema (POST) para deshabilitar su notificación
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notifications";
                        if (!task.isSuccessful()){
                            msg = "UnSubscription failed";
                        }
                        Toast.makeText(AjustesActivity2.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }//ahora, en AddPostActivity cuando el usuario publica una publicación envía una notificación con el mismo tema "POST"

    private void subscribePostNotification() {
        //suscribirse a un tema (POST) para habilitar su notificación
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive post notifications";
                        if (!task.isSuccessful()){
                            msg = "Subscription failed";
                        }
                        Toast.makeText(AjustesActivity2.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}