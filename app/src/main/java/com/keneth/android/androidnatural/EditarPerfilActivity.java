package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.keneth.android.androidnatural.modelos.ModelUser;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class EditarPerfilActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ImageView close, image_profile;
    TextView save, tv_change;
    Button SelecionImagBtn;
    MaterialEditText nombre, telefono, ubicacion;

    //Ubicaciom
    int a;
    String item;
    TextView textView;
    ModelUser usuario;
    Spinner spinner1;
    //Ubicaciom
    FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;

//el que es
//firebase
    FirebaseAuth firebaseAuth;
    //FirebaseUser user;  user = firebaseUser
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String uid;

    //RUTA donde se almacenarán las imágenes del perfil de usuario y la portada
    String storagePath = "Users_Profile_Cover_Imgs/";
    //para consultar foto de perfil o portada
    String profileOrCoverPhoto = "image";              //CORREGIR ESTO
    //diálogo de progreso
    ProgressDialog pd;
//el que es
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        //v_change = findViewById(R.id.tv_change);
        SelecionImagBtn = findViewById(R.id.SelecionImagBtn);
        nombre = findViewById(R.id.nombre);
        telefono = findViewById(R.id.telefono);
        ubicacion = findViewById(R.id.ubicacion);


        //Ubicaciom
        spinner1 = findViewById(R.id.spinner1);                                  //contexto,       elementos a mostrar,          tipo de spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.ciudades, R.layout.spinner_item_ubicacion);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);
        //Ubicaciom

        //inicializacion firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();   //firebaseDatabase = FirebaseDatabase.getInstance();   //databaseReference = firebaseDatabase.getReference("Users");
        databaseReference = firebaseDatabase.getReference("Users");
        storageRef = getInstance().getReference(); //referencia de almacenamiento de base de fuego

        //inicialisamos el dialogo de progreso
        //pd = new ProgressDialog(getActivity());

        //sin nombre2
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //storageRef = FirebaseStorage.getInstance().getReference("uploads");
        //sin nombre2

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);
                nombre.setText(user.getName());
                telefono.setText(user.getPhone());     //traigo la info que tengo guardada del usuario
                ubicacion.setText(user.getLocation());
                Glide.with(getApplicationContext()).load(user.getImage())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_profile_black)) //VIDEO 20             //PUESTO 09/06/2021
                        .into(image_profile);

                         //.into(image_profile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditarPerfilActivity.this);
            }
        });*/
        SelecionImagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditarPerfilActivity.this);
            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditarPerfilActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pd.setMessage("Modificando nombre...");

                /*updateProfile(nombre.getText().toString(),  //le damos el valor de lo que ingresa en el EditText a la funcion de modificar
                                        telefono.getText().toString());*///,
                //                        //bio.getText().toStrin));

                //elques
                showNamePhoneUpdateDialog(nombre.getText().toString(),
                        telefono.getText().toString(),ubicacion.getText().toString(),"name");
                //elquees

                //xd
                /*startActivity(new Intent(EditarPerfilActivity.this, PerfilFragment.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));*/
                //xd

            }

        });
        checkUserStatus();


    }

    private void updateProfile(String nombre, String telefono) {//, String bio
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")//instanciamiento del Obj FirebaseDatabase
                .child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>(); //creamos Mapa de valores (HashMap)
        hashMap.put("name", nombre);
        hashMap.put("phone", telefono);     //que queremos en la BD
        //hashMap.put("bio", bio);

        //le ponemos el mapa al obj DataBase que esta referenciado con (reference)
        reference.updateChildren(hashMap);

    }

    //EL QUE ES
    //MODIFICAR NOMBRE Y TELEFONO
    private void showNamePhoneUpdateDialog(String nombre, String telefono, String ubicacion, String key) {
        /*el parámetro "clave" contendrá el valor:
         * ya sea "nombre" que es clave en la base de datos del usuario que se utiliza para actualizar el nombre del usuario
         * o "teléfono" que es clave en la base de datos del usuario que se utiliza para actualizar el teléfono del usuario*/


                //introducir texto desde editar texto
                //String value = nombre;
                    //pd.show();
                    /*HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);*/
                     HashMap<String, Object> result = new HashMap<>();
                     result.put("name", nombre);
                     result.put("phone", telefono);


                     //xd
                     if (item == ""){
                         Toast.makeText(EditarPerfilActivity.this, "porfavor seleccione una opcion", Toast.LENGTH_SHORT).show();
                     }else {
                         //post.setPalabra(item);
                         result.put("location", ubicacion);
                     }
                     //xd


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")//instanciamiento del Obj FirebaseDatabase
                .child(firebaseUser.getUid());

        //databaseReference.child(firebaseUser.getUid()).updateChildren(result)
        reference.updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //actualizar, descartar progreso
//                                    pd.dismiss();
                                    Toast.makeText(EditarPerfilActivity.this, "Modificado, (USERS)", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //falló, descartar el progreso, obtener y mostrar mensaje de error
                                    pd.dismiss();
                                    Toast.makeText(EditarPerfilActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });




        //TODOLOQUEMETIEN onsucces

                        //SI el usuario edita su nombre, cámbielo también de sus PUBLICACIONES HISTORICAS
        if (key.equals("name")) {
            //lista de publicaciones de inicio
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            /*Siempre que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación.
             * por lo que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
            Query query = ref.orderByChild("uid").equalTo(uid); //uid del usuario
            //obtener todos los datos de esta referencia
            query.addListenerForSingleValueEvent(new ValueEventListener() {//addValueEventListener
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String child = ds.getKey(); //las unicas publicaciones que he hecho como user actual (1620530724640)
                        snapshot.getRef().child(child).child("uName").setValue(nombre);
                        snapshot.getRef().child(child).child("uLocation").setValue(ubicacion);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            //actualizar el nombre de los usuarios actuales en los comentarios en las publicaciones
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Comments");

            ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String child = ds.getKey(); //todos los COMENTARIOS que estan en "Comments"

                        //String child1 = ""+snapshot.child(child).getKey();
                        //Toast.makeText(getActivity(), "es2: "+child1, Toast.LENGTH_SHORT).show();
                        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(postId); //crea comentario como sinnombre2
                        //Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                        Query child2 = FirebaseDatabase.getInstance().getReference("Comments").child(child).orderByChild("uid").equalTo(uid);
                        //Toast.makeText(getActivity(), "es55: "+child2, Toast.LENGTH_SHORT).show();
                        child2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    //Toast.makeText(getActivity(), "este: "+child, Toast.LENGTH_SHORT).show();
                                    snapshot.getRef().child(child).child("uName").setValue(nombre);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
    //ELQUE ES

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    //funcion modificar sinnombre2
    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (mImageUri != null){
            final StorageReference filereference  = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadTask = filereference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return filereference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("image", "" + myUrl);

                        reference.updateChildren(hashMap);
                        pd.dismiss();
                    }else {
                        Toast.makeText(EditarPerfilActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditarPerfilActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "No selecciono una imagen", Toast.LENGTH_SHORT).show();
        }
    }
    //funcion modificar sinnombre2

    //funcion modificar elquees
    private void uploadProfileCoverPhoto(final Uri uri) {
        //show progress
        //pd.show();

        /*En lugar de crear una función separada para la foto de perfil y la foto de portada
         * Estoy trabajando para ambos en la misma función
         *
         * Para agregar una marca, agregaré una variable de cadena y le asignaré el valor "imagen" cuando el usuario haga clic
         * "Editar foto de perfil", y asígnele el valor "portada" cuando el usuario haga clic en "Editar foto de portada"
         * Aquí: la imagen es la clave en cada usuario que contiene la URL de la imagen de perfil del usuario
         * portada es la clave en cada usuario que contiene la URL de la foto de portada del usuario*/

        /*El parámetro "image_url * contiene el uri de la imagen seleccionada de la cámara o la galería
         * usaremos el UID del usuario actualmente registrado como nombre de la imagen, por lo que solo habrá una imagen
         * perfil y una imagen de portada para cada usuario*/

        //ruta y nombre de la imagen que se almacenará en firebase storage
        //e.g Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        //e.g Users_Profile_Cover_Imgs/image_e123n4567g89.jpg
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ firebaseUser.getUid(); //firebaseUser = "user"

        StorageReference storageReference2nd = storageRef.child(filePathAndName); //storageRef = "storageReference"
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //la imagen se carga en el almacenamiento, ahora obtenga su URL y almacénela en la base de datos del usuario
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //comprobar si la imagen está cargada o no y se recibe la URL
                        if (uriTask.isSuccessful()){
                            //imagen cargada
                            //agregar/actualizar la URL en la base de datos del usuario
                            HashMap<String, Object> results = new HashMap<>();
                            /*El primer parámetro es profileOrCoverPhoto que tiene el valor "imagen" o "portada".
                             * que comió claves en la base de datos del usuario donde la URL de la imagen se guardará en una
                             * de ellos
                             * El segundo parámetro contiene la URL de la imagen almacenada en firebase storage, este
                             * la URL se guardará como valor contra la clave "imagen" o "portada"*/
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(firebaseUser.getUid()).updateChildren(results)  ////firebaseUser = "user"
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL en la base de datos del usuario se agrega con éxito
                                            //descartar la barra de progreso
                                            //pd.dismiss();
                                            Toast.makeText(EditarPerfilActivity.this, "Imagen Modificada...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error al agregar URL en la base de datos del usuario
                                            //descartar la barra de progreso
                                            pd.dismiss();
                                            Toast.makeText(EditarPerfilActivity.this, "Error al Modificar Imagen de Perfil...", Toast.LENGTH_SHORT).show();


                                        }
                                    });

                            //si el usuario edita su imagen, cámbielo también de sus PUBLICACIONES HISTORICAS
                            if (profileOrCoverPhoto.equals("image")){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //actualizar la imagen del usuario en los COMENTARIOS de los usuarios actuales en las publicaciones

                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Comments");
                                //ref
                                ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            //if (snapshot.child(child).hasChild("Comments")){

                                            String child1 = ""+snapshot.child(child).getKey();
                                            //Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                            Query child2 = FirebaseDatabase.getInstance().getReference("Comments").child(child).orderByChild("uid").equalTo(uid);
                                            child2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds: snapshot.getChildren()){
                                                        String child = ds.getKey();
                                                        snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                            // }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        else{
                            //error
                            pd.dismiss();
                            Toast.makeText(EditarPerfilActivity.this, "\n" + "Ocurrió algún error!", Toast.LENGTH_SHORT).show();
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //allí donde algunos errores, obtener y mostrar un mensaje de error, descartar el diálogo de progreso
                        pd.dismiss();
                        Toast.makeText(EditarPerfilActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }
    //funcion modificar elquees

    private void checkUserStatus(){
        //Obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //El usuario ha iniciado sesión aquí
            //establecer el correo electrónico del usuario que inició sesión
            //  mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        }
        else{
            //el usuario no ha iniciado sesión, vaya a MainActivity
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            //uploadImage();
            uploadProfileCoverPhoto(mImageUri);

        }else {
            Toast.makeText(this, "something gone worng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /*item = spinner1.getSelectedItem().toString();
        textView.setText(item);*/

        item = parent.getItemAtPosition(position).toString();
        ubicacion.setText(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}