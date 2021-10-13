package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100 ;
    GoogleSignInClient mGoogleSignInClient;

    //views
    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccentTv, mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;

    //Declare una instancia de FirebaseAuth
    private FirebaseAuth mAuth;

    //progress dialog
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Actionbar y su título
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Iniciar Sesion");
        //habilitar el botón de retroceso
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //ANTES mAuth
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //En el método onCreate (), inicialice la instancia de FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        //inicializacion
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccentTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);


        //login Button Click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //los datos de entrada
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //no es valido el email paatern set error
                    mEmailEt.setError("Correo Invalido!");
                    mEmailEt.setFocusable(true);
                }
                else{
                    //el correo es valido
                    loginUser(email, passw);
                }
            }
        });
        //no tener cuenta textview click
        notHaveAccentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrarActivity.class));
                finish();
            }
        });

        //recuperar texto pasar vista clic
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //manejar google login btn click
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //comenzar el proceso de inicio de sesión de Google
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //inicializamos progress dialog
        pd = new ProgressDialog(this);
    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");  //antes "Recover Password"

        //establecer layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        //vista para establecer en diálogo
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Correo");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        /*establece el ancho mínimo de un EditView para que se ajuste a un texto de n letras 'M' independientemente del texto real
        extensión y tamaño del texto*/
        emailEt.setMinEms(16);


        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //Botones recuperar
        builder.setPositiveButton("Recuperar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //entrada email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        //Botones cancelar
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //descartar diálogo
                dialog.dismiss();
            }
        });

        //mostrar dialog
        builder.create().show();

    }

    private void beginRecovery(String email) {
        //mostrar diálogo de progreso
        pd.setMessage("Sending email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email enviado", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Fallo el envio...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //obtener y mostrar el mensaje de error adecuado
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();//mirar
            }
        });
    }

    private void loginUser(String email, String passw) {
        //mostrar diálogo de progreso
        pd.setMessage("Entrando...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //descartar diálogo de progreso
                            pd.dismiss();
                            // Iniciar sesión correctamente, actualizar la interfaz de usuario con la información del usuario que inició sesión
                            FirebaseUser user = mAuth.getCurrentUser();
                            //El usuario ha iniciado sesión, así que inicie LoginActivity
                            startActivity(new Intent(LoginActivity.this, TableroActivity.class));
                            finish();
                        } else {
                            //descartar diálogo de progreso
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Autenticacion fallida!",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //descartar diálogo de progreso
                pd.dismiss();
                //error, obtener y mostrar mensaje de error
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //vamos al activity anterior
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado devuelto al iniciar Intent desde GoogleSignInApi.getSignInIntent (...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // El inicio de sesión de Google se realizó correctamente, autentíquese con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Error de inicio de sesión de Google, actualice la interfaz de usuario correctamente
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Iniciar sesión correctamente, actualizar la interfaz de usuario con la información del usuario que inició sesión

                            FirebaseUser user = mAuth.getCurrentUser();

                            //si el usuario inicia sesión por primera vez, obtenga y muestre la información del usuario de la cuenta de Google
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
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
                            }



                            //mostrar el correo electrónico del usuario en un toast
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            //ir a perfilActivity después de iniciar sesión
                            startActivity(new Intent(LoginActivity.this, TableroActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Fallo el inicio de sesion...", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //obtener y mostrar mensaje de error
                Toast.makeText(LoginActivity.this, "LOKENDO"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
