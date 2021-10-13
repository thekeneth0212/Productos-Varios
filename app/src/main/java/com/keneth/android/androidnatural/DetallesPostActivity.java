package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.keneth.android.androidnatural.adapter.AdapterComments;
import com.keneth.android.androidnatural.modelos.ModelComment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DetallesPostActivity extends AppCompatActivity {

    //para obtener detalles del usuario y la publicación
    String  hisUid, myUid, myEmail, myName, myDp,
    postId, pLikes, hisDp, hisName, pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    //progress bar
    ProgressDialog pd;


    //views
    ImageView uPictureIv, pImageIv, like,likeBtn,shareBtn,estadoIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    //Button likeBtn, shareBtn;
    //Button shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //agregar vista de comentarios
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_post);

        //Actionbar y sus propiedades
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Detalles Publicacion");                  //TITULO
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //obtener el id de la publicación usando la intención (intent)
        Intent intent =  getIntent();
        postId = intent.getStringExtra("postId");      //este id es utilizado en AdapterNotificaions 111

        //init views
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        //like = findViewById(R.id.like);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profilelayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        estadoIv = findViewById(R.id.estadoIv);


        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();



        //sin nombre2
        nrLikes(pLikesTv, postId);
        getComments(postId, pCommentsTv);
        //sinnombre2

        //establecer subtítulos de la barra de acción (actionbar)
        //actionBar.setSubtitle("Usuario: "+myEmail);


        loadComments();

        //enviar comentario clic en el botón
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //megusta  clic en el botón de la manija
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });


        //(...)más botón controlador de clic
       //quitarBoton();


        //RECICLADO
        estadoIv.setImageResource(R.drawable.ic_estado_sinreciclar);
        //compruebe si cada publicacion esta reciclada o no
        checkIsBlocked(postId);
        //RECICLADO

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //showMoreObtions();

                //PRUEBA ESTADO
                //creando un menú emergente que actualmente tiene la opción Eliminar, agregaremos más opciones más adelante
                PopupMenu popupMenu = new PopupMenu(DetallesPostActivity.this,moreBtn, Gravity.END);

                //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
                if (hisUid.equals(myUid)){
                    //agregar elementos en el menú
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar publicación");        //importante menu
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicación");


                    //PRUEBA FUNCIONA

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(postId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                    int a=0;
                                    for (DataSnapshot ds: snapshot.getChildren()){

                                        if (ds.exists()){
                                            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Publicar articulo nuevamente");
                                            a=1;
                                        }
                                    }
                                    if (a!=1) {
                                        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Articulo entregado");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull  DatabaseError error) {

                                }
                            });

                    //PRUEBA FUNCIONA



                }

                //elemento de escucha de clics
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id==0){
                            //se hace clic en eliminar
                            beginDelete();
                        }
                        else if (id==1){
                            //se hace clic en Edit
                            //inicie AddPostActivity con la clave "editPost" y la identificación de la publicación en la que se hizo clic
                            Intent intent = new Intent(DetallesPostActivity.this, AddPostActivity.class);
                            intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                            intent.putExtra("editPostId", postId);      //utilizado en AddPostActivity
                            startActivity(intent);
                        }
                        else if (id==2){
                            //esatado post
                            //PRUEBA FUNCIONA

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(postId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                            int a=0;
                                            for (DataSnapshot ds: snapshot.getChildren()){

                                                if (ds.exists()){
                                                    //Toast.makeText(context, "mostrar 1: " +a, Toast.LENGTH_SHORT).show();
                                                    unBlockUser(postId);
                                                    a = 1;
                                                }
                                            }
                                            //Toast.makeText(context, "mostrar 2" , Toast.LENGTH_SHORT).show();
                                            if (a != 1){

                                                blockUser(postId);
                                            }


                                        }

                                        @Override
                                        public void onCancelled(@NonNull  DatabaseError error) {

                                        }
                                    });

                            //PRUEBA FUNCIONA






                        }

                        return false;
                    }
                });
                //Muestrame el menu
                popupMenu.show();
                //PRUEBA ESTADO
            }
        });

        //botón compartir haga clic en la manija
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();

                //obtener la imagen de la vista de imagen (imageview)
                BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                if (bitmapDrawable == null){
                    //publicar sin imagen
                    shareTextOnly(pTitle, pDescription,  hisUid, postId);
                }
                else {
                    //publicar con imagen

                    //convertir imagen a mapa de bits (convert image to bitmap)
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap,  hisUid, postId);
                }
            }
        });

        //click en likes
        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetallesPostActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });

        uPictureIv.setOnClickListener(new View.OnClickListener() { //cambie uPictureIv por profileLayout
            @Override
            public void onClick(View v) {
                /*haga clic para ir a ThereProfileActivity con uid, este uid es del usuario que hizo clic
                 * que se utilizará para mostrar datos / publicaciones específicos del usuario*/
                if (myUid.equals(hisUid) ){
                    Toast.makeText(DetallesPostActivity.this, "uid: "+hisUid, Toast.LENGTH_SHORT).show();
                    Toast.makeText(DetallesPostActivity.this, "myUid: "+myUid, Toast.LENGTH_SHORT).show();

                }
                else {

                    Intent intent = new Intent(DetallesPostActivity.this, ThereProfileActivity.class);
                    intent.putExtra("uid",hisUid);
                    startActivity(intent);
                }
            }
        });

    }

    private void checkIsBlocked(String postId) {
        //comprobar cada usuario, si está bloqueado o no
        //si el uid del usuario existe en "BlockedUsers", ese usuario está bloqueado, de lo contrario no

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int a=0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                estadoIv.setImageResource(R.drawable.ic_estado_reciclado);
                                a = 1;
                            }
                        }
                        if (a!=1) {
                            estadoIv.setImageResource(R.drawable.ic_estado_sinreciclar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String postId) {
        //bloquear al usuario, agregando uid al nodo "RecyclerPosts" del usuario actual

        // poner valores en hasmap para poner en db
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("pId", postId);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").child(postId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //bloqueado con éxito
                        Toast.makeText(DetallesPostActivity.this, "Reciclado con exito...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //no se pudo bloquear
                        Toast.makeText(DetallesPostActivity.this, "Fallo: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser(String postId) {
        //desbloquear al usuario, eliminando uid del nodo "RecyclerPosts" del usuario actual

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                //eliminó los datos del post reciclado de la lista de usuarios reciclados del usuario actual
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Desbloqueo con exitoso
                                                Toast.makeText(DetallesPostActivity.this, "Puesto nuevamente...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //no se pudo desbloquear
                                                Toast.makeText(DetallesPostActivity.this, "Fallo: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
    }

    private void addToHisNotifications(String hisUid, String pId, String notification){
        String timesTamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timesTamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timesTamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //agregado exitosamente

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //fallo
                    }
                });
    }

    private void shareTextOnly(String pTitle, String pDescription,  String uid, String pId) {
        //concatenar título y descripción para compartir
        String shareBody = pTitle +"\n"+ pDescription;

        //compartir intención (intent)
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //en caso de que comparta a través de una aplicación de correo electrónico
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //texto para compartir
        startActivity(Intent.createChooser(sIntent, "Share Via")); //mensaje para mostrar en el cuadro de diálogo para compartir

        //No enviar notificacion a uno mismo
        /*if (uid.equals(myUid)){
            Toast.makeText(context, "Funcionn!", Toast.LENGTH_SHORT).show();
        }
        else {
            addToHisNotifications(""+uid, ""+pId, "Compartio tu publicación");
        }*/
        //No enviar notificacion a uno mismo

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap,  String uid, String pId) {
        //concatenar título y descripción para compartir
        String shareBody = pTitle +"\n"+ pDescription;

        //primero guardaremos esta imagen en la caché, obtendremos la uri de la imagen guardada
        Uri uri = saveImageToShare(bitmap);

        //compartir intencion (intent)
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share Via"));
        //copiar el mismo código en PostDetailActivity

        //No enviar notificacion a uno mismo
        /*if (uid.equals(myUid)){
            Toast.makeText(DetallesPostActivity.this, "Funcionn!", Toast.LENGTH_SHORT).show();
        }
        else {
            addToHisNotifications(""+uid, ""+pId, "Compartio tu publicación");
        }*/
        //No enviar notificacion a uno mismo


    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //crear si no existe
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.keneth.android.androidnatural.fileprovider",
                    file);
        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }


    private void loadComments() {
        //para que aparezca la reciente
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //mostrar la publicación más reciente primero, para esta carga desde la última
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        //establecer diseño(layout) en recyclerview
        recyclerView.setLayoutManager(layoutManager);
        //para que aparezca la reciente

        //layout(Linear) for recyclerview diseño (lineal) para la vista del reciclador
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recyclerview establecer diseño en vista de reciclaje
        //recyclerView.setLayoutManager(layoutManager);

        //lista de comentarios de inicio
        commentList = new ArrayList<>();

        //ruta de la publicación, para obtener sus comentarios
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments"); //MIRAR
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(postId); //crea comentario como sinnombre2
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    //pasar myUid y PostId como parámetro del constructor del Adaptador de comentarios


                    //adaptador de instalación
                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    //establecer adaptador
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreObtions() {
        //creando un menú emergente que actualmente tiene la opción Eliminar, agregaremos más opciones más adelante
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
        if (hisUid.equals(myUid)){
            //agregar elementos en el menú
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar publicación");        //importante menu
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicación");
        }

        //elemento de escucha de clics
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    //se hace clic en eliminar
                    beginDelete();
                    //que se vaya para inicio
                    startActivity(new Intent(DetallesPostActivity.this, TableroActivity.class));
                }
                else if (id==1){
                    //se hace clic en Edit
                    //inicie AddPostActivity con la clave "editPost" y la identificación de la publicación en la que se hizo clic
                    Intent intent = new Intent(DetallesPostActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                    intent.putExtra("editPostId", postId);      //utilizado en AddPostActivity
                    startActivity(intent);
                }
                return false;
            }
        });
        //Muestrame el menu
        popupMenu.show();
    }

    private void quitarBoton(){
        //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
        if (hisUid.equals(myUid)){
            Toast.makeText(this, "mi post", Toast.LENGTH_SHORT).show();
        }else
        {
            moreBtn.setBottom(R.drawable.ic_close);
        }

        //establecer datos
        /*try {
            //si se recibe la imagen el conjunto
            Picasso.get().load(myDp).placeholder(R.drawable.ic_profile_black).into(cAvatarIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_profile_black).into(cAvatarIv);
        }*/
    }

    private void beginDelete() {
        //la publicación puede ser con o sin imagen

        if (pImage.equals("noImage")){
            //la publicación no tiene imagen
            deleteWithoutImage();
        }
        else{
            //la publicación es con imagen
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        //barra de progreso
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        /*Pasos:
         * 1) Eliminar imagen usando url
         * 2) Eliminar de la base de datos usando la identificación de publicación*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //imagen eliminada, ahora eliminada la base de datos

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue(); //eliminar valores de la firebase donde el pid coincide
                                }
                                //eliminado
                                Toast.makeText(DetallesPostActivity.this, "Deleted Successfully (eliminado)", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //falló, no puedo ir más lejos
                        pd.dismiss();
                        Toast.makeText(DetallesPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        //barra de progreso
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue(); //eliminar valores de la firebase donde el pid coincide
                }
                //eliminado
                Toast.makeText(DetallesPostActivity.this, "Deleted Successfully (eliminado)", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        //cuando se cargan los detalles de la publicación, también verifique si al usuario actual le ha gustado o no
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)){
                    //Al usuario le ha gustado esta publicación.
                    /* Para indicar que a este usuario (SigneIn) le gusta la publicación
                    Cambiar el icono dibujable de la izquierda del botón Me gusta
                    Cambiar el texto del botón Me gusta de "Me gusta" a "Me gustó"*/
                    //likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);//IMPORTANTE
                    likeBtn.setImageResource(R.drawable.ic_meencanta_green);
                    //likeBtn.setText("Me gusto");
                }
                else {
                    //Al usuario NO le ha gustado esta publicación.
                    /* Para indicar que a este usuario (SigneIn) NO le gusta la publicación
                    Cambiar el icono dibujable de la izquierda del botón Me gusta
                    Cambiar el texto del botón Me gusta de "Me gusto" a "Me gusta"*/
                    //likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);//IMPORTANTE
                    likeBtn.setImageResource(R.drawable.ic_meencanta_white);
                    //likeBtn.setText("Me gusta");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void likePost() {
        //obtener el número total de Me gusta de la publicación, cuyo botón Me gusta hizo clic
        //si el usuario que ha iniciado sesión actualmente no le ha gustado antes
        //aumenta el valor en 1, de lo contrario disminuye el valor en 1
        mProcessLike = true;
        //obtener la identificación de la publicación en la que se hizo clic
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike){
                    if (snapshot.child(postId).hasChild(myUid)){
                        //ya me gustó, así que elimine me gusta
                        //postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));  //IMPORTANTE
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

                    }
                    else {
                        //no me gustó, me gusta
                        //postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); //establecer cualquier valor
                        mProcessLike = false;

                        //No enviar notificacion a uno mismo
                        if (hisUid.equals(myUid)){
                            //Toast.makeText(DetallesPostActivity.this, "Funciona2! ", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            addToHisNotifications("" + hisUid, "" + postId, "Le gusto tu publicación");
                        }
                        //No enviar notificacion a uno mismo

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Comentando...");

        //obtener datos del comentario editar texto (edit text)
        String comment = commentEt.getText().toString().trim();
        //validacion
        if (TextUtils.isEmpty(comment)){
            //no se ingresa ningún valor
            Toast.makeText(this, "Comentario vacio!", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //Cada publicación tendrá un hijo "Comments" que contendrá comentarios de esa publicación
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(postId); //crea comentario como sinnombre2

        HashMap<String, Object> hashMap = new HashMap<>();
        //poner información en hashmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        //poner estos datos en db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //agregar
                        pd.dismiss();
                        //Toast.makeText(DetallesPostActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        //updateCommentCount();



                        //No enviar notificacion a uno mismo
                        if (hisUid.equals(myUid)){
                            //Toast.makeText(DetallesPostActivity.this, "Funcionn!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            addToHisNotifications(""+hisUid,""+postId,"Comento tu publicación");
                        }
                        //No enviar notificacion a uno mismo
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Fallido, no agregado
                        pd.dismiss();
                        Toast.makeText(DetallesPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void updateCommentCount() {
        //siempre que el usuario agregue un comentario, aumente el recuento de comentarios como lo hicimos para el recuento de me gusta
        mProcessComment = true;
        DatabaseReference ref=  FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment){
                    String comments = ""+ snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadUserInfo() {
        //obtener informacion de usuario actual
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    //establecer datos
                    try {
                        //si se recibe la imagen el conjunto
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_profile_black).into(cAvatarIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_profile_black).into(cAvatarIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //obtener publicación usando la identificación de la publicación
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //siga revisando las publicaciones hasta obtener la publicación requerida
                for (DataSnapshot ds: snapshot.getChildren()){
                    //obteners datos
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();

                    //convertir la marca de tiempo en el formato adecuado
                    //convertir la marca de tiempo a dd / mm / aaaa hh: mm am / pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //establecer datos
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + " Me gusta");
                    pTimeTiv.setText(pTime);
                    //pCommentsTv.setText(commentCount);

                    uNameTv.setText(hisName);


                    //Establecer imagen del usuario que publicó
                    //si no hay una imagen, es decir, pImage.equals ("noImage"), oculte ImageView
                    if (pImage.equals("noImage")){
                        //ocultar imageview
                        pImageIv.setVisibility(View.GONE);
                    }
                    else {
                        //mostrar imageview
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        }
                        catch (Exception e){

                        }
                    }

                    //establecer la imagen de usuario en la parte de comentario
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_profile_black).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_profile_black).into(uPictureIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!= null){
            //La usuario ha iniciado sesión
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else {
            //La usuario NO ha iniciado sesión, ir a la actividad principal
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //ocultar algunos elementos del menú
        menu.findItem(R.id.action_add_post).setVisible(false);  //oculatr item del menu
        menu.findItem(R.id.action_search).setVisible(false);

        //ocultar el icono de addpost de este fragmento
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_settings2).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_prueba).setVisible(false);
        menu.findItem(R.id.action_contraseña).setVisible(false);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener ID de item
        int id = item.getItemId();
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void nrLikes(final TextView likes, String postid){   //funcion numero de likes

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes") //nombre tabla
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                likes.setText(dataSnapshot.getChildrenCount() + " Me gusta");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //prueba liked sin ombre2

    //prueba numero comentarios sinnombre2
    private void getComments(String postid, final TextView comments){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);//BD

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                comments.setText( dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //prueba numero comentarios sinnombre2
}
