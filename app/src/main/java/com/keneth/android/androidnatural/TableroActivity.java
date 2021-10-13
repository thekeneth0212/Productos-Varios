package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.keneth.android.androidnatural.notifications.Token;

public class TableroActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    //views

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablero);

        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.app);
        //Actionbar y su título
        actionBar = getSupportActionBar();
        actionBar.setTitle("Perfil");*/

        //inicializacion
        firebaseAuth = FirebaseAuth.getInstance();

        //Boton Navegador
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //transacción de fragmentos de perfil (predeterminado al INICIO)
        //actionBar.setTitle("Inicio");//cambiar el título de la barra de acción
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        //VIDEO 10
                                                                                                                            //QUITAR POR SI ALGO
        Bundle intent = getIntent().getExtras();
        if (intent != null){

            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);                             //PROBLEMA HP perfilid           LO CAMBIE 22-09-20
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.content,
                    new PerfilFragment()).commit();

        }else{

            getSupportFragmentManager().beginTransaction().replace(R.id.content,
                    new HomeFragment()).commit();
        }

        //VIDEO 10

        checkUserStatus();



    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //manejar clics de elementos
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            //transacción de fragmentos de perfil
                            //actionBar.setTitle("Inicio");//cambiar el título de la barra de acción
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //transacción de fragmentos de Perfil
                            //actionBar.setTitle("Perfil");//cambiar el título de la barra de acción
                            PerfilFragment fragment2 = new PerfilFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //transacción de fragmentos de usuarios
                            //actionBar.setTitle("Android Natural");//cambiar el título de la barra de acción
                            UsuariosFragment fragment3 = new UsuariosFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            //transacción de fragmentos de usuarios
                            //actionBar.setTitle("Android Natural");//cambiar el título de la barra de acción
                            ChatListaFragment fragment4 = new ChatListaFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;
                        case R.id.nav_notification:
                            //transacción de fragmentos de usuarios
                            //actionBar.setTitle("Android Natural");//cambiar el título de la barra de acción
                            NotificacionesFragment fragment5 = new NotificacionesFragment();
                            FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                            ft5.replace(R.id.content, fragment5, "");
                            ft5.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void checkUserStatus(){
        //Obtener usuario actua
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //El usuario ha iniciado sesión aquí
            //establecer el correo electrónico del usuario que inició sesión
            //  mProfileTv.setText(user.getEmail());
            mUID = user.getUid();

            //guardar uid del usuario que ha iniciado sesión actualmente en preferencias compartidas
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //token de actualización
            updateToken(FirebaseInstanceId.getInstance().getToken());

        }
        else{
            //el usuario no ha iniciado sesión, vaya a MainActivity
            startActivity(new Intent(TableroActivity.this, SplashScreenActivity.class));   //Cambie 25/06/2021
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //comprobar en la aplicación de inicio
        checkUserStatus();
        super.onStart();
    }

    //boton atras (PARA CERRAR LA app)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == event.KEYCODE_BACK){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea salir de AndroidNatural?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
        return super.onKeyDown(keyCode, event);
    }
}
