package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddTextPostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    ActionBar actionBar;

    //constantes de permisos
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //constantes de selección de imágenes
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    //views
    EditText titleEt, descriptionEt;                       //edt_descripcion
    MaterialEditText categoryEt;
    Spinner spinner1;
    String item;                                //imagen_agregar
    Button uploadBtn;                                      //post

    //Información de usuario
    String name, email, location, uid, dp;

    //info of post to be edited
    String editTitle, editDescription, editImage;

    //la imagen elegida se guardará en este uri
    Uri image_rui = null;                                      //imageUri

    //barra de progreso
    ProgressDialog pd;

    //xd
    EditText edt_titulo;
    int a;

    //add imagen sin nombre
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;


    TextView tv_change;
    //add imagen sin nombre

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Nueva Publicación");
        //habilitar el botón de retroceso en la barra de acciones
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //inicializamos array de los permisos
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //iniciamos view
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        uploadBtn = findViewById(R.id.pUploadBtn);
        categoryEt = findViewById(R.id.categoria);

        //Categoria
        spinner1 = findViewById(R.id.spinner1);                                  //contexto,       elementos a mostrar,          tipo de spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.categorias, R.layout.spinner_item_ubicacion);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);
        //Categoria

        //xd
        edt_titulo = findViewById(R.id.pTitleEt);

//add imagen sin nombre
        //tv_change = findViewById(R.id.tv_change);
//add imagen sin nombre

        //obtener datos a través de la intención del adaptador de la actividad anterior
        Intent intent = getIntent();

//32    //obtener datos y su tipo a partir de la intención
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type!=null){

            if ("text/plain".equals(type)){
                //datos de tipo de texto
                handleSendText(intent);            }
            else if (type.startsWith("image")){
                //datos de tipo de imagen
                //handleSendImage(intent);
            }

        }

        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        Toast.makeText(this, "esto: "+isUpdateKey, Toast.LENGTH_SHORT).show();
        //validar si vinimos aquí para actualizar la publicación, es decir i.e. vino de AdapterPost
        if (isUpdateKey.equals("editPost")){
            //modificar
            actionBar.setTitle("Modificar Publicación");
            uploadBtn.setText("Guardar");
            loadPostData(editPostId);

        }
        else {
            //agregar
            actionBar.setTitle("Nueva Publicación");
            uploadBtn.setText("Publicar");

            //Luego de publicar se va al home
            /*startActivity(new Intent(AddPostActivity.this, HomeFragment.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));*/
        }

        actionBar.setSubtitle(email);

        //obtener información del usuario actual para incluir en la publicación
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    name = ""+ ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();

                    location = ""+ ds.child("location").getValue();

                    dp = ""+ ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //botón de carga haga clic en oyente
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obtener datos (título, descripción) de edittext

                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                String category = categoryEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddTextPostActivity.this, "El titulo NO puede estar vacio", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddTextPostActivity.this, "La descripción NO puede estar vacia", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(category)){
                    Toast.makeText(AddTextPostActivity.this, "Seleccione la categoria", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")){
                    beginUpdate(title, description,category, editPostId);
                }
                else {
                    uploadData(title, description, category);
                }


            }
        });

        //xd1
        edt_titulo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (a==0) {
                    showDialog1();
                    a=a+1;
                }
                return false;
            }
        });
        //xd1

//add imagen sin nombre


        /*CropImage.activity()
                .setAspectRatio(1, 1)
                .start(AddPostActivity.this);*/

//add imagen sin nombre

    }

    private void showDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddTextPostActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_titulo, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button btnVale = view.findViewById(R.id.btnvale);
        btnVale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "vale", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void uploadData(String title, String description, String category) {
        pd.setMessage("Publicando...");
        pd.show();

        //para el nombre de la imagen posterior, la identificación posterior, la hora posterior a la publicación
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

            //publicacion sin imagen
            HashMap< Object, String> hashMap = new HashMap<>();
            //poner información de la publicación
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);

            hashMap.put("uLocation", location);

            hashMap.put("uDp", dp );
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");
            //xd
            if (item == ""){
                Toast.makeText(AddTextPostActivity.this, "porfavor seleccione una categoria", Toast.LENGTH_SHORT).show();
            }else {
                //post.setPalabra(item);
                hashMap.put("pCategory", category);
            }
            //xd

            //ruta para almacenar datos de publicaciones
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PostsText");
            //poner datos en esta referencia
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //agregado en la base de datos
                            pd.dismiss();
                            Toast.makeText(AddTextPostActivity.this, "Articulo compartido!", Toast.LENGTH_SHORT).show();
                            //que se vaya para inicio
                            startActivity(new Intent(AddTextPostActivity.this, TableroActivity.class));

                            //restablecer vistas
                            titleEt.setText("");
                            descriptionEt.setText("");
                            categoryEt.setText("");
                            //imageIv.setImageURI(null);
                            image_rui = null;

                            //enviar notificacion
                            prepareNotification(
                                    ""+timeStamp,//ya que estamos usando la marca de tiempo para la identificación de la publicación
                                    ""+name+ " added new post",
                                    ""+title+"\n"+description,
                                    "PostNotification",
                                    "POST"  //es TOPIC_POST_NOTIFICATION = "POST"
                            );

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //no se pudo agregar la publicación en la base de datos
                            pd.dismiss();
                            Toast.makeText(AddTextPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
        //preparar datos para notificación
        String NOTIFICATION_TOPIC = "/topics/"+ notificationTopic; //El tema debe coincidir con lo que el receptor está suscrito.
        String NOTIFICATION_TITLE = title; //e.g Atif Pervaiz added new post
        String NOTIFICATION_MESSAGE = description; //content of post
        String NOTIFICATION_TYPE = notificationType; //ahora hay dos tipos de notificación, chat y publicación, para diferenciar en la clase FirebaseMessaging.java

        //preparar json qué enviar y dónde enviar
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //que enviar
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid); //uid de uso actual / remitente
            notificationBodyJo.put("pId", pId); //ID del mensaje
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
            //donde enviar
            notificationJo.put("to", NOTIFICATION_TOPIC);

            notificationJo.put("data", notificationBodyJo);//combinar datos para enviar
        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
        //enviar solicitud de objeto de volea (volley)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponde: "+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //error ocurred
                        Toast.makeText(AddTextPostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //poner encabezados requeridos
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAsuSJ5SE:APA91bFtYJDs57uirr3jJPLqCmyUkGHBXT8OxYcJ-u-59OQ3w4AS-eOcMV7_OeaxlcI6FL8Yka7yD_5N9ahMp2qMiigJhVE6pd1yL-LduqXvC9VFMz83GqeSHTt_PW7c16cLHq3ndGUW");//pegue su clave fcm aquí después de "key="

                return headers;
            }
        };
        //poner en cola la solicitud de volea
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


    private void beginUpdate(String title, String description, String category, String editPostId) {
        pd.setMessage("Actualizando publicacion...");
        pd.show();



            //sin imagen
            updateWithoutImage(title, description,category, editPostId);

        Toast.makeText(AddTextPostActivity.this, "Articulo actualizado!", Toast.LENGTH_SHORT).show();
        //que se vaya para inicio
        //startActivity(new Intent(AddPostActivity.this, TableroActivity.class));
    }

    private void updateWithoutImage(String title, String description, String category, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        //poner información de la publicación
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);

        hashMap.put("uLocation", location);

        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        //xd
        if (item == ""){
            Toast.makeText(AddTextPostActivity.this, "porfavor seleccione una categoria", Toast.LENGTH_SHORT).show();
        }else {
            //post.setPalabra(item);
            hashMap.put("pCategory", category);
        }
        //xd
        hashMap.put("pImage", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PostsText");  //POSTS
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddTextPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddTextPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PostsText"); //POSTS
        //obtener detalles de la publicación usando la identificación de la publicación
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //optener datos
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescr").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    //establecer datos en vistas
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //establecer imagen

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleSendText(Intent intent) {
        //manejar la imagen recibida (uri)
        Uri imageURI = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null){
            image_rui = imageURI;
            //establecer en vista de imagen
            //imageIv.setImageURI(image_rui);
        }
    }

    private void checkUserStatus() {
        //Obtener usuario actua
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //El usuario ha iniciado sesión aquí
            email = user.getEmail();
            uid = user.getUid();

        }
        else{
            //el usuario no ha iniciado sesión, vaya a MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        item = parent.getItemAtPosition(i).toString();
        categoryEt.setText(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}