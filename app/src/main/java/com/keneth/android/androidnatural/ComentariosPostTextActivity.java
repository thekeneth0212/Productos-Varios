package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.keneth.android.androidnatural.adapter.AdapterComments;
import com.keneth.android.androidnatural.modelos.ModelComment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ComentariosPostTextActivity extends AppCompatActivity {


    //para obtener detalles del usuario y la publicación
    //la que es
    TextView post;

    String  hisUid, myUid, myEmail, myName, myDp,
            postId, pLikes, hisDp, hisName, pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    //progress bar
    ProgressDialog pd;


    //views
    ImageView uPictureIv, pImageIv, like;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //agregar vista de comentarios
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;
    ImageView image_profile;
    //la que es
//xd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios_post_text);

        //xd
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //xd


        //Actionbar y sus propiedades
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Comentarios");                  //TITULO
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //opciones bonot
        //obtener el id de la publicación usando la intención (intent)
        Intent intent = getIntent();
        //postid = intent.getStringExtra("postId");            //mio "pId"  / postId     //que es "postid"
        postId = intent.getStringExtra("postId");            //mio "pId"  / postId     //que es "postid"
        //publisherid = intent.getStringExtra("publisherid");  //mio ""         //que es "publisherid"
        //opciones boton

        post = findViewById(R.id.post);


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
        //like = findViewById(R.id.like);
        profileLayout = findViewById(R.id.profilelayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();



        //setLikes();

        //establecer subtítulos de la barra de acción (actionbar)
        actionBar.setSubtitle("Usuario: "+myEmail);

        loadComments();
        //la que es

        //post
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if (addcomment.getText().toString().equals("")){

                    Toast.makeText(ComentariosActivity2.this, "No puedes enviar un comentario vacio!", Toast.LENGTH_SHORT).show();
                }else{

                    addComment();
                }*/
                postComment();
            }
        });

        cAvatarIv.setOnClickListener(new View.OnClickListener() { //cambie uPictureIv por profileLayout
            @Override
            public void onClick(View v) {
                /*haga clic para ir a ThereProfileActivity con uid, este uid es del usuario que hizo clic
                 * que se utilizará para mostrar datos / publicaciones específicos del usuario*/
                if (myUid.equals(hisUid) ){

                }
                else {

                    Intent intent = new Intent(ComentariosPostTextActivity.this, ThereProfileActivity.class);
                    intent.putExtra("uid",hisUid);
                    startActivity(intent);
                }
            }
        });

    }


//NOTIFICACION EL QUE ES

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
    } //NO SIRVE

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



    //private void loadComments() {
    //layout(Linear) for recyclerview diseño (lineal) para la vista del reciclador
    //LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
    //xd
    /*    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //xd
        //set layout to recyclerview establecer diseño en vista de reciclaje
        recyclerView.setLayoutManager(layoutManager);

        //lista de comentarios de inicio
        commentList = new ArrayList<>();

        //ruta de la publicación, para obtener sus comentarios
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments"); //MIRAR
        //xd
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(postid);;
        //xd
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    //pasar myUid y PostId como parámetro del constructor del Adaptador de comentarios


                    //adaptador de instalación
                    //adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    //xd
                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    //xd
                    //establecer adaptador
                    recyclerView.setAdapter(adapterComments);
                }
                //xd
                adapterComments.notifyDataSetChanged();
                //xd
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    //agregar comentario EL QUE ES
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
                        //Toast.makeText(ComentariosActivity2.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        //updateCommentCount();

                        Toast.makeText(ComentariosPostTextActivity.this, "esto: "+hisUid, Toast.LENGTH_SHORT).show();
                        //No enviar notificacion a uno mismo
                        if (hisUid.equals(myUid)){
                            Toast.makeText(ComentariosPostTextActivity.this, "Funcionn!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            addToHisNotifications(""+hisUid,""+postId,"Comento tu Artículo");
                        }
                        //No enviar notificacion a uno mismo
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Fallido, no agregado
                        pd.dismiss();
                        Toast.makeText(ComentariosPostTextActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
    //agregar comentario EL QUE ES
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
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_profile_black).into(cAvatarIv);//cAvatarIv
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_profile_black).into(cAvatarIv);//cAvatarIv
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PostsText");
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

                    //Toast.makeText(ComentariosPostTextActivity.this, "HISUID "+ hisUid, Toast.LENGTH_SHORT).show();

                    //convertir la marca de tiempo en el formato adecuado
                    //convertir la marca de tiempo a dd / mm / aaaa hh: mm am / pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //establecer datos
                  /*  pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + " Me gusta");
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText(commentCount + " Comentarios");

                    uNameTv.setText(hisName);*/


                    //Establecer imagen del usuario que publicó
                    //si no hay una imagen, es decir, pImage.equals ("noImage"), oculte ImageView
          /*          if (pImage.equals("noImage")){
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
                    }*/

                    //establecer la imagen de usuario en la parte de comentario
                   /*try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img).into(uPictureIv);
                    }*/
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


//NOTIFICACION EL QUE ES
}