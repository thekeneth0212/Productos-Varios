package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistrarActivity extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    //barra de progreso para mostrar al registrar al usuario
    ProgressDialog progressDialog;

    //Declare una instancia de FirebaseAuth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        //Actionbar y su título
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Crear Cuenta");
        //habilitar el botón de retroceso
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //inicializamos
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        //En el método onCreate (), inicialice la instancia de FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //manejar registro btn clic
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //entrada de correo electrónico, contraseña
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //validacion
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //establecer el error y los enfoques para editar texto por correo electrónico
                    mEmailEt.setError("Correo Invalido");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6){
                    //establecer el error y los enfoques para editar texto por password
                    mPasswordEt.setError("Contraseña menor a 6 caracteres");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email,password);//funcion para registrar el usuario
                }

            }
        });
        //manejar el texto de inicio de sesión
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrarActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        //email and password es válido, muestra el diálogo de progreso y comienza a registrar al usuario
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Iniciar sesión correctamente, descartar el diálogo e iniciar la actividad de registro
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //Obtener el uid y el correo electrónico del usuario de autenticación (auth)
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //cuando el usuario está registrado, también almacena información en la base de datos en tiempo real de firebase
                            // usando HashMap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            // poner información en hasmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");// Lo agregaré más tarde (por ejemplo, editprofile)
                            hashMap.put("onlineStatus", "online");// Lo agregaré más tarde (por ejemplo, editprofile)
                            hashMap.put("typingTo", "noOne");// Lo agregaré más tarde (por ejemplo, editprofile)
                            hashMap.put("phone", "");// Lo agregaré más tarde (por ejemplo, editprofile)
                            hashMap.put("image", "");// Lo agregaré más tarde (por ejemplo, editprofile)
                            hashMap.put("cover", "");// Lo agregaré más tarde (por ejemplo, editprofile)
                            hashMap.put("location", "");// Lo agregaré más tarde (por ejemplo, editprofile)

                            //base de datos firebase isntance
                            FirebaseDatabase database= FirebaseDatabase.getInstance();
                            //ruta para almacenar datos de usuario denominados "Usuarios"
                            DatabaseReference reference = database.getReference("Users");
                            //poner datos dentro de hashmap en la base de datos
                            reference.child(uid).setValue(hashMap);



                            Toast.makeText(RegistrarActivity.this, "Registrado...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistrarActivity.this, TableroActivity.class));
                            finish();
                        } else {
                            //Si el inicio de sesión falla, muestra un mensaje al usuario.
                            progressDialog.dismiss();
                            Toast.makeText(RegistrarActivity.this, "Autenticacion Fallida!",Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, descarte el diálogo de progreso y obtenga y muestre el mensaje de error
                progressDialog.dismiss();
                Toast.makeText(RegistrarActivity.this, "Correro ya registrado!", Toast.LENGTH_SHORT).show();
                //Toast.makeText(RegistrarActivity.this, "perro: "+e.getMessage(), Toast.LENGTH_SHORT).show();                          //ARREGLAR(15/04/2021)
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //vamos al activity anterior
        return super.onSupportNavigateUp();
    }
}
