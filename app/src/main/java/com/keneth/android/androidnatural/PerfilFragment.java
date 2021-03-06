package com.keneth.android.androidnatural;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keneth.android.androidnatural.adapter.AdapterMyFotos;
import com.keneth.android.androidnatural.adapter.AdapterPosts;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //almacenamiento
    StorageReference storageReference;
    //RUTA donde se almacenar??n las im??genes del perfil de usuario y la portada
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views del xml
    ImageView avatarIv, coverIv, opcionesIv,add_postIv;
    CircularImageView avatarIv1;
    TextView nameTv, emailTv, phoneTv, locationTv;
    SearchView svSearch;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView;


    String profileid;
    //mostrar recycler de a 3fv

    //saved
    private List<String> mySaves;
    //saved

    //di??logo de progreso
    ProgressDialog pd;

    //constantes de permisos
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //conjunto de permisos a solicitar
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    //SOLO IMAGEN
    List<ModelPost> postList2;
    AdapterMyFotos adapterMyFotos;
    //SOLO IMAGEN
    String uid;
    String ubicacion;

    //uri de imagen elegida
    Uri image_uri;

    //para consultar foto de perfil o portada
     String profileOrCoverPhoto;

    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //nose
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none"); //no me cambia
        //nose


        //inicializacion firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();   //firebaseDatabase = FirebaseDatabase.getInstance();   //databaseReference = firebaseDatabase.getReference("Users");
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //referencia de almacenamiento de base de fuego

        //inicializacion arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //iniciali8zamos los views
        avatarIv = view.findViewById(R.id.avatarIv);
        //avatarIv = view.findViewById(R.id.avatarIv);
        //coverIv = view.findViewById(R.id.coverIv);
        opcionesIv = view.findViewById(R.id.options);
        add_postIv = view.findViewById(R.id.add_postIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        locationTv = view.findViewById(R.id.locationTv);
        //svSearch = (SearchView) view.findViewById(R.id.buscarSv);
        //fab = view.findViewById(R.id.fab);
        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);
       /* recyclerView = view.findViewById(R.id.recyclear_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        adapterMyFotos = new AdapterMyFotos(getContext(), postList);
        recyclerView.setAdapter(adapterMyFotos);*/

        //inicialisamos el dialogo de progreso
        pd = new ProgressDialog(getActivity());

        /*tenemos que obtener informaci??n del usuario actualmente registrado, podemos obtenerlo usando el correo electr??nico o uid del usuario
          Voy a recuperar los detalles del usuario usando el correo electr??nico * /
        / * Al usar la consulta orderByChild mostraremos el detalle de un nodo
          cuya clave denominada correo electr??nico tiene un valor igual al correo electr??nico registrado actualmente.
          Buscar?? todos los nodos, donde la clave coincida obtendr?? su detalle*/
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //checkc hasta obtener los datos requeridos
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();
                    String location = ""+ ds.child("location").getValue();
                    ubicacion = ""+ ds.child("location").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    locationTv.setText(location);
                    try {
                        //si se recibe la imagen, establezca
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        //si hay alguna excepci??n al obtener la imagen, establezca el valor predeterminado
                        Picasso.get().load(R.drawable.ic_profile_black).into(avatarIv);
                    }

                    try {
                        //si se recibe la imagen, establezca
                        //Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        //si hay alguna excepci??n al obtener la imagen, establezca el valor predeterminado
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //fab button click
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });*/

        /*svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el bot??n de b??squeda
                if (!TextUtils.isEmpty(query)) {
                    searchMyPosts(query);
                } else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //llamado cuando el usuario presiona cualquier letra
                if (!TextUtils.isEmpty(newText)) {
                    searchMyPosts(newText);
                } else {
                    loadMyPosts();
                }
                return false;
            }
        });*/

        opcionesIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ir a ajustes activity
                startActivity(new Intent(getActivity(), OpcionesActivity.class));
            }
        });

        add_postIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ir a AddPost activity
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });




        postList = new ArrayList<>();
        postList2 = new ArrayList<>();

        checkUserStatus();
        loadMyPosts();                                                                                      //searchMyPosts

        //myFotos();

        return view;
    }

    //CARGAR LOS POST
    private void loadMyPosts() {
        //linear layout for recyclerview(dise??o lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());                                                //Normal
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);                                      //2 Columnas
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);   //2 columnas bien                       //2 Columnas
        //mostrar la publicaci??n m??s reciente primero, para esta carga desde la ??ltima                       IMPORTANTE
        layoutManager.setStackFromEnd(true);
        gridLayoutManager.setReverseLayout(true);
        //set this layout to recyclerview(establezca este dise??o en vista de reciclaje)
        postsRecyclerView.setLayoutManager(gridLayoutManager);
        postsRecyclerView.setHasFixedSize(true);

        //lista de publicaciones de inicio
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //(query)consulta para cargar publicaciones
        /*Siempre que el usuario publica una publicaci??n, el uid de este usuario tambi??n se guarda como informaci??n de la publicaci??n.
         * por lo que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //obtener todos los datos de esta referencia
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//1                postList.clear();
                postList2.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //agregar a la lista
//1                        postList.add(myPosts);
                    postList2.add(myPosts);
                    //adaptador (adapter)
//1                    adapterPosts = new AdapterPosts(getActivity(), postList); //postList
                    adapterMyFotos = new AdapterMyFotos(getActivity(), postList2); //postList
                    //set this adapter to recyclerview
//1                    postsRecyclerView.setAdapter(adapterPosts);
                    postsRecyclerView.setAdapter(adapterMyFotos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();                    //QUITE 24/05/2021
            }
        });
    }
    //BUSCAR LOS POST
    private void searchMyPosts(String searchQuery) {
        //linear layout for recyclerview(dise??o lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);                                      //2 Columnas
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);   //2 columnas bien                       //2 Columnas

        //mostrar la publicaci??n m??s reciente primero, para esta carga desde la ??ltima                       IMPORTANTE
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview(establezca este dise??o en vista de reciclaje)
        postsRecyclerView.setLayoutManager(layoutManager);

        //lista de publicaciones de inicio
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //(query)consulta para cargar publicaciones
        /*Siempre que el usuario publica una publicaci??n, el uid de este usuario tambi??n se guarda como informaci??n de la publicaci??n.
         * por lo que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //obtener todos los datos de esta referencia
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){                          //IMPORTANTE
                        //agregar a la lista
                        postList.add(myPosts);
                    }

                    //adaptador (adapter)
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //PERMISOS
    private boolean checkStoragePermissions(){
        //verifique que el permiso de almacenamiento est?? habilitado o no
        // devuelve verdadero si est?? habilitado
        // devuelve falso si no est?? habilitado
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    //PERMISOS
    private void requestStoragePermission(){
        //solicitar permiso de almacenamiento en tiempo de ejecuci??n
        requestPermissions( storagePermissions, STORAGE_REQUEST_CODE);
    }

    //PERMISOS
    private boolean checkCameraPermissions(){
        //verifique que el permiso de almacenamiento est?? habilitado o no
        // devuelve verdadero si est?? habilitado
        // devuelve falso si no est?? habilitado
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    //PERMISOS
    private void requestCameraPermission(){
        //solicitar permiso de almacenamiento en tiempo de ejecuci??n
        requestPermissions( cameraPermissions, CAMERA_REQUEST_CODE);
    }

//BOTONREDONDO
    private void showEditProfileDialog() {
         /*Mostrar di??logo que contiene opciones:
                * 1) Editar imagen de perfil
                * 2) Editar foto de portada
                * 3) Editar nombre
                * 4) Editar tel??fono
                * 5) Cambiar Contrase??a*/

         //opciones para mostrar en el di??logo
        //String options[] = {"Editar Foto de Perfil","Editar Foto de Portada","Editar Nombre","Editar Telefono", "Cambiar Contrase??a"};
        String options[] = {"Editar Foto de Perfil","Editar Nombre","Editar Telefono", "Cambiar Contrase??a"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Men??");
        //establecer elementos en di??logo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog iten clicks
                if (which == 0){
                    //Editar perfil hecho clic
                    pd.setMessage("Actualizando imagen de perfil...");
                    profileOrCoverPhoto = "image";//es decir, cambiar la imagen de perfil, aseg??rese de asignar el mismo valor
                    showImagePicDialog();
                    /*CropImage.activity()
                            .setAspectRatio(1, 1)
                            .start(getActivity());*/
                }
                /*else if (which == 1){
                    //Editar cover hecho clic
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";//es decir, cambiar la foto de portada, aseg??rese de asignar el mismo valor
                    showImagePicDialog();
                }*/
                else if (which == 1){
                    //Editar Nombre hecho clic
                    pd.setMessage("Modificando nombre...");
                    //m??todo de llamada y clave de paso "nombre" como par??metro para actualizar su valor en la base de datos
                    showNamePhoneUpdateDialog("name");//name
                }
                else if (which == 2){
                    //Editar telefono hecho clic
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
                else if (which == 3){
                    //Editar Contrase??a hecho clic
                    pd.setMessage("Changing Password");
                    //showChangePasswordDialog();
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }
//BOTONREDONDO

//CONTRASE??A
    /*
    private void showChangePasswordDialog() {
        //di??logo de cambio de contrase??a con dise??o personalizado que tiene currentPassword, newPassword y bot??n de actualizaci??n

        //inflar el dise??o para el di??logo
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_modificar_contrase, null);
        EditText passwordEt = view.findViewById(R.id.passwordEt);
        EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); //establecer vista en di??logo

        AlertDialog dialog = builder.create();
        dialog.show();


        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validar datos
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "introduce tu contrase??a actual...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), "Contrase??a menor a 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
       pd.show();

        //Obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //antes de cambiar la contrase??a, vuelva a autenticar al usuario
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //autenticaci??n con ??xito, comenzar la actualizaci??n

                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Contrase??a actualiza
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Contrase??a Actualizada...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //fallo la actualizacion de la contrase??a, mostrar razon
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //fallo la autenticaci??n, mostrar la raz??n
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    */
 //CONTRASE??A


//MODIFICAR NOMBRE Y TELEFONO
    private void showNamePhoneUpdateDialog(final String key) {
        /*el par??metro "clave" contendr?? el valor:
         * ya sea "nombre" que es clave en la base de datos del usuario que se utiliza para actualizar el nombre del usuario
         * o "tel??fono" que es clave en la base de datos del usuario que se utiliza para actualizar el tel??fono del usuario*/

        //persnalizar dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Modificar "+ key);//e.g Actualizar nombre O Actualizar tel??fono
        //builder.setTitle("Modificar "+ key);
        //establecer el dise??o del di??logo
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Ingresar ");//hint //e.g Editar nombre O Editar tel??fono
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //agregar butto en el di??logo para actualizar
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //introducir texto desde editar texto
                String value = editText.getText().toString().trim();
                //validar si el usuario ha ingresado algo o no
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                                                                       //firebaseDatabase = FirebaseDatabase.getInstance();
                                                                       //databaseReference = firebaseDatabase.getReference("Users");

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //actualizar, descartar progreso
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Modificado...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //fall??, descartar el progreso, obtener y mostrar mensaje de error
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    //si el usuario edita su nombre, c??mbielo tambi??n de sus publicaciones hist??ricas
                    if (key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
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
                                                    Toast.makeText(getActivity(), "este: "+child, Toast.LENGTH_SHORT).show();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
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



                         /*   DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Comments");
                            Query query1 = ref1.orderByChild("uid").equalTo(uid);
                            query1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        String child = ds.getKey();
                                        snapshot.getRef().child(child).child("uName").setValue(value);
                                    }
                                }
                                //Query child2 = FirebaseDatabase.getInstance().getReference("Comments").child(child1).orderByChild("uid").equalTo(uid);

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });*/



                    }
                }
                else {
                    Toast.makeText(getActivity(), "\n" + "Por favor escribe..."+key, Toast.LENGTH_SHORT).show();
                }


            }
        });
        //agregar butto en el di??logo para cancelar
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //crear y mostrar di??logo
        builder.create().show();

    }

    private void showImagePicDialog() {
        //show dialog containing options Camera and Gallery to pick the image

        String options[] = {"Camara","Galeria"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Selecciona");
        //establecer elementos en di??logo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog iten clicks
                if (which == 0){
                    //clic en Camara

                    if (!checkCameraPermissions()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    //clic en Galeria
                    if (!checkStoragePermissions()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();

        /*para elegir la imagen de:
        * C??mara {Se requiere permiso de c??mara y almacenamiento}
        * Galer??a {Se requiere permiso de almacenamiento}*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*Este m??todo se llama cuando el usuario presiona permitir o Denegar desde el cuadro de di??logo de solicitud de permiso
        * aqu?? manejaremos casos de permisos (permitidos y denegados) */

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                // seleccionando desde la c??mara, primero verifique si los permisos de c??mara y almacenamiento est??n permitidos o no
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //permisos habilitados
                        pickFromCamera();
                    }
                    else {
                        //permisos denegados
                        Toast.makeText(getActivity(), "Por favor habilite los permisos de camara y memoria", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                // seleccionando desde la galeria, primero verifique si los permisos de almacenamiento est??n permitidos o no
                if (grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //permisos habilitados
                        pickFromGallery();
                    }
                    else {
                        //permisos denegados
                        Toast.makeText(getActivity(), "Por favor habilite los permisos de memoria", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*Este m??todo se llamar?? despu??s de seleccionar la imagen de la c??mara o la galer??a*/
        if (resultCode == RESULT_OK){

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //la imagen se elige de la galera, obtenga el uri de la imagen
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //La imagen se toma de la c??mara

                uploadProfileCoverPhoto(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);

        /*super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            //CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //image_uri = result.getUri();
            image_uri = data.getData();

            //imageIv.setImageURI(image_rui);
            uploadProfileCoverPhoto(image_uri);

        }else{

            Toast.makeText(getActivity(), "algo salio mal!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), TableroActivity.class));

        }*/
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        //show progress
        pd.show();

        /*En lugar de crear una funci??n separada para la foto de perfil y la foto de portada
         * Estoy trabajando para ambos en la misma funci??n
         *
         * Para agregar una marca, agregar?? una variable de cadena y le asignar?? el valor "imagen" cuando el usuario haga clic
         * "Editar foto de perfil", y as??gnele el valor "portada" cuando el usuario haga clic en "Editar foto de portada"
         * Aqu??: la imagen es la clave en cada usuario que contiene la URL de la imagen de perfil del usuario
         * portada es la clave en cada usuario que contiene la URL de la foto de portada del usuario*/

        /*El par??metro "image_url * contiene el uri de la imagen seleccionada de la c??mara o la galer??a
        * usaremos el UID del usuario actualmente registrado como nombre de la imagen, por lo que solo habr?? una imagen
        * perfil y una imagen de portada para cada usuario*/

        //ruta y nombre de la imagen que se almacenar?? en firebase storage
        //e.g Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        //e.g Users_Profile_Cover_Imgs/image_e123n4567g89.jpg
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //la imagen se carga en el almacenamiento, ahora obtenga su URL y almac??nela en la base de datos del usuario
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //comprobar si la imagen est?? cargada o no y se recibe la URL
                        if (uriTask.isSuccessful()){
                            //imagen cargada
                            //agregar/actualizar la URL en la base de datos del usuario
                            HashMap<String, Object> results = new HashMap<>();
                            /*El primer par??metro es profileOrCoverPhoto que tiene el valor "imagen" o "portada".
                             * que comi?? claves en la base de datos del usuario donde la URL de la imagen se guardar?? en una
                             * de ellos
                             * El segundo par??metro contiene la URL de la imagen almacenada en firebase storage, este
                             * la URL se guardar?? como valor contra la clave "imagen" o "portada"*/
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL en la base de datos del usuario se agrega con ??xito
                                            //descartar la barra de progreso
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Imagen Modificada...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error al agregar URL en la base de datos del usuario
                                            //descartar la barra de progreso
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error al Modificar Imagen de Perfil...", Toast.LENGTH_SHORT).show();


                                        }
                                    });

                            //si el usuario edita su imagen, c??mbielo tambi??n de sus PUBLICACIONES HISTORICAS
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
                            Toast.makeText(getActivity(), "\n" + "Ocurri?? alg??n error!", Toast.LENGTH_SHORT).show();
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //all?? donde algunos errores, obtener y mostrar un mensaje de error, descartar el di??logo de progreso
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void pickFromCamera() {
        //Intenci??n de tomar una imagen de la c??mara del dispositivo
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //poner imagen uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intenci??n de iniciar la c??mara
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleyIntent = new Intent(Intent.ACTION_PICK);
        galleyIntent.setType("image/*");
        startActivityForResult(galleyIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void checkUserStatus(){
        //Obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //El usuario ha iniciado sesi??n aqu??
            //establecer el correo electr??nico del usuario que inici?? sesi??n
            //  mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        }
        else{
            //el usuario no ha iniciado sesi??n, vaya a MainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//para mostrar la opci??n de men?? en un fragmento
        super.onCreate(savedInstanceState);
    }

    /*men?? de opciones de inflar*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //no mostrar en el MENU
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_prueba).setVisible(false);
        menu.findItem(R.id.action_contrase??a).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchview para buscar publicaciones espec??ficas del usuario
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el bot??n de b??squeda
                if (!TextUtils.isEmpty(query)){
                    //search
                    searchMyPosts(query);                                                                                      //searchMyPosts
                }
                else {
                    loadMyPosts();                                                                                      //loadpost
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //llamado cada vez que el usuario escribe cualquier letra
                if (!TextUtils.isEmpty(s)){
                    //search
                    searchMyPosts(s);                                                                                      //searchMyPosts
                }
                else {
                    loadMyPosts();                                                                                      //loadpost
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    /*manejar los clics en los elementos del men??*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(getActivity(), OpcionesActivity.class));
        //obtener ID de item

        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if (id==R.id.action_settings){
            //ir a ajustes activity
            startActivity(new Intent(getActivity(), AjustesActivity2.class));
        }
        //else
            if (id==R.id.action_settings2){
            //ir a ajustes activity
            startActivity(new Intent(getActivity(), OpcionesActivity.class));
        }
        else if (id==R.id.action_prueba){
            //ir a ajustes activity
            startActivity(new Intent(getActivity(), EditarPerfilActivity.class));
        }
        else if (id==R.id.action_contrase??a){
            //ir a ajustes activity
            pd.setMessage("Changing Password");
            //showChangePasswordDialog();
        }
        return super.onOptionsItemSelected(item);

    }

    //imagenes de a 3
    private void myFotos(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    ModelPost post = snapshot.getValue(ModelPost.class);
                    if(post.getUid().equals(profileid)){   //getUid = getpublisher

                        postList.add(post);

                    }

                }
                Collections.reverse(postList);
                adapterMyFotos.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //imagenes de a 3
}
