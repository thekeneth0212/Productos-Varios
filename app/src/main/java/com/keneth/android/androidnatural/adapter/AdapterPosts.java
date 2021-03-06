package com.keneth.android.androidnatural.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.storage.UploadTask;
import com.keneth.android.androidnatural.AddPostActivity;
import com.keneth.android.androidnatural.AddTextPostActivity;
import com.keneth.android.androidnatural.ComentariosActivity1;
import com.keneth.android.androidnatural.ComentariosActivity2;
import com.keneth.android.androidnatural.DetallesPostActivity;
import com.keneth.android.androidnatural.ForoActivity;
import com.keneth.android.androidnatural.PostLikedByActivity;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.TableroActivity;
import com.keneth.android.androidnatural.ThereProfileActivity;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {


    Context context;
    List<ModelPost> postList;

    //para obtener el uid del usuario actual
    FirebaseAuth firebaseAuth;
    String myUid;
    //String uid,pId;

    private DatabaseReference likesRef; //para el nodo de la base de datos de Me gusta
    private DatabaseReference postsRef; //referencia de puestos

    boolean mProcessLike = false;

    //xd
    private FirebaseUser firebaseUser;
    //xd

    int editar=0;
    //barra de progreso
    ProgressDialog pd;


    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");        //IMPORTANTE
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        firebaseAuth = FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflar dise??o row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //xd
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//
        final ModelPost post = postList.get(position);//

        //sinnombre2
        //Picasso.get().load(uDp).placeholder(R.drawable.placeholder).into(holder.pImageIv);
           Glide.with(context).load(post.getpImage())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder)) //VIDEO 20
                    .into(holder.pImageIv);
        //sinnombre2

        //xd
        //obtener datos
         String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();  //uid, uName, uEmail, uLocation, uDp, 8 pTimeStamp, pTitle, pDescription, pImage, pTimeStamp
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pCatecory = postList.get(position).getpCategory();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes(); //contiene el n??mero total de me gusta para un post
        String pComments = postList.get(position).getpComments(); //contiene el n??mero total de comentarios para un post

        String uLocation = postList.get(position).getuLocation();


        //convertir la marca de tiempo a dd / mm / aaaa hh: mm am / pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //establecer datos
//MODI        holder.uNameTv.setText(uName);
//MODI        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
//MODI        holder.pDescriptionTv.setText(pDescription);
//MODI        holder.pCategoriaTv.setText(pCatecory);
        holder.pLikesTv.setText(pLikes); //p.ej. 100 me gusta
        //holder.pCommentsTv.setText(pComments+ " Comentarios"); //p.ej. 100 me gusta
        //establecer Me gusta para cada publicaci??n
         setLikes(holder, pId);

        //pruena likes sin nomrebe2
        //isLiked(post.getpId(), holder.like);
        nrLikes(holder.pLikesTv, post.getpId());
        //pruena likes sin nomrebe2

        pd = new ProgressDialog(context);

        //sinnombre
        //getComments(pId, holder.pCommentsTv);
        //getComments(post.getPostid(), holder.comments);
        getComments(post.getpId(), holder.pCommentsTv);
        //sinnombrer

        //establecer dp de usuario
        try {
//MODI            Picasso.get().load(uDp).placeholder(R.drawable.ic_profile_black).into(holder.uPictureIv);

        }
        catch (Exception e){

        }

        //establecer imagen de publicaci??n
        //si no hay una imagen, es decir, pImage.equals ("noImage"), oculte ImageView
        if (pImage.equals("noImage")){
            //ocultar imageview
            holder.pImageIv.setVisibility(View.GONE);
        }
        else {
            //mostrar imageview
            holder.pImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch (Exception e){

            }
        }

        //RECICLADO
        holder.estadoIv.setImageResource(R.drawable.ic_estado_sinreciclar);
        //compruebe si cada publicacion esta reciclada o no
        checkIsBlocked(pId, holder, position);
        //RECICLADO
        //No image
//24/07/21        checkIsNoImage(pId, holder, position,pImage);
        //No image

        //compruebe si cada publicacion esta guardada
        isSaved(post.getpId(), holder.saveIv);
        //compruebe si cada publicacion esta guardada

        //manejar Bot??n clic
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //showMoreObtions(holder.moreBtn, uid, myUid, pId, pImage);

                clickMoreBtn(uid,holder.moreBtn, pId, pImage,holder.pImageIv, pTitle,pDescription, holder.saveIv); //holder.pImageIv,pTitle, pDescription, uid, pId
                //PRUEBA ESTADO
                //creando un men?? emergente que actualmente tiene la opci??n Eliminar, agregaremos m??s opciones m??s adelante
              /*  PopupMenu popupMenu = new PopupMenu(context,holder.moreBtn, Gravity.END);

                //mostrar la opci??n de eliminar solo en las publicaciones del usuario que ha iniciado sesi??n actualmente
                if (uid.equals(myUid)){
                    //agregar elementos en el men??
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar publicaci??n");        //importante menu
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicaci??n");


                    //PRUEBA FUNCIONA

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
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
                popupMenu.getMenu().add(Menu.NONE, 3, 0, "Ver detalles");

                //elemento de escucha de clics
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id==0){
                            //se hace clic en eliminar
                            beginDelete(pId, pImage);
                        }
                        else if (id==1){
                            //se hace clic en Edit
                            //inicie AddPostActivity con la clave "editPost" y la identificaci??n de la publicaci??n en la que se hizo clic
                            Intent intent = new Intent(context, AddPostActivity.class);
                            intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                            intent.putExtra("editPostId", pId);      //utilizado en AddPostActivity
                            context.startActivity(intent);
                        }
                        else if (id==2){
                            //esatado post
                            //PRUEBA FUNCIONA

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                    ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                                    int a=0;
                                                    for (DataSnapshot ds: snapshot.getChildren()){

                                                        if (ds.exists()){
                                                            //Toast.makeText(context, "mostrar 1: " +a, Toast.LENGTH_SHORT).show();
                                                            unBlockUser(pId);
                                                            a = 1;
                                                        }
                                                    }
                                                    //Toast.makeText(context, "mostrar 2" , Toast.LENGTH_SHORT).show();
                                                    if (a != 1){

                                                        blockUser(pId);
                                                    }


                                                }

                                                @Override
                                                public void onCancelled(@NonNull  DatabaseError error) {

                                                }
                                            });

                            //PRUEBA FUNCIONA






                        }
                        else if (id==3){
                            //start DetallesPostActivity
                            Intent intent = new Intent(context, DetallesPostActivity.class);
                            intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                            context.startActivity(intent);
                        }
                        return false;
                    }
                });
                //Muestrame el menu
                popupMenu.show();*/
                //PRUEBA ESTADO
            }
        });

        //likes el que es
       holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obtener el n??mero total de Me gusta de la publicaci??n, cuyo bot??n Me gusta hizo clic
                //si el usuario que ha iniciado sesi??n actualmente no le ha gustado antes
                //aumenta el valor en 1, de lo contrario disminuye el valor en 1
                int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                //obtener la identificaci??n de la publicaci??n en la que se hizo clic
                String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike){
                            if (snapshot.child(postIde).hasChild(myUid)){
                                //ya NO me gust??, as?? que elimine me gusta
                                //postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));  //IMPORTANTE
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else {
                                //no me gust??, me gusta
                                //postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked"); //establecer cualquier valor
                                mProcessLike = false;

                                //No enviar notificacion a uno mismo
                                if (uid.equals(myUid)){
                                    //Toast.makeText(context, "Funciona2! ", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    addToHisNotifications("" + uid, "" + pId, "Le gusto tu publicaci??n");
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
        });

       holder.likeBtn.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View v) {
               Intent intent = new Intent(context, PostLikedByActivity.class);
               intent.putExtra("postId", pId);
               context.startActivity(intent);
               return false;
           }
       });
       //pruena likes sin nomrebe2
       /* holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //funcion like (oprimir estrellita)

                if(holder.like.getTag().equals("like")){  //este like que?

                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getpId())
                            .child(firebaseUser.getUid()).setValue(true);

                    //notificacion el que es
                    addToHisNotifications(""+uid, ""+pId, "Liked your post");
                    //notificacion el que es

                }else{

                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getpId()) //realodeBDtabla
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });*/
        //pruena likes sin nomrebe2

        //GUARDAR post
        holder.saveIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.saveIv.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(pId).setValue(true);
                }
                else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(pId).removeValue();
                }
            }
        });
        //GUARDAR post

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start DetallesPostActivity
    //el que es
               /*Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                context.startActivity(intent);*/
    //el que es
                //sinnombre2
                Intent intent = new Intent(context, ComentariosActivity2.class);    //ese (pId) es = post.getPostid()
                intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                intent.putExtra("publisherid", pImage);   //ese (pImage) es = post.getPublisher()
                context.startActivity(intent);

                /*Intent intent = new Intent(mContext, ComentariosActivity.class);
                intent.putExtra("postid", post.getPostid());           //nose
                intent.putExtra("publisherid", post.getPublisher());   //nose
                mContext.startActivity(intent);*/
                //sinnombre2

            }
        });
//MODI        holder.pCommentsTv.setOnClickListener(new View.OnClickListener() {
           /* @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ComentariosActivity2.class);    //ese (pId) es = post.getPostid()
                intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                intent.putExtra("publisherid", pImage);   //ese (pImage) es = post.getPublisher()
                context.startActivity(intent);

            }
//MODI       });*/

        /*holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickSharePost(holder.pImageIv,pTitle, pDescription, uid, pId);

                /*algunas publicaciones contienen solo texto, y algunas contienen imagen y texto, por lo que las manejaremos a ambas*/
                //obtener la imagen de la vista de imagen (imageview)
             /*   BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.pImageIv.getDrawable();
                if (bitmapDrawable == null){

                    //compartir SIN imagen
                    shareTextOnly(pTitle, pDescription, uid, pId);

                }
                else {

                    //compartir CON imagen

                    //convertir imagen a mapa de bits (convert image to bitmap)
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap,  uid, pId);

                }
            }
        });*/

        //MODI /*       holder.uPictureIv.setOnClickListener(new View.OnClickListener() { //cambie uPictureIv por profileLayout
    /*        @Override
           public void onClick(View v) {
                /*haga clic para ir a ThereProfileActivity con uid, este uid es del usuario que hizo clic
                 * que se utilizar?? para mostrar datos / publicaciones espec??ficos del usuario*/
             /*   if (myUid.equals(uid) ){*/
                    //Toast.makeText(context, "uid: "+uid, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, "myUid: "+myUid, Toast.LENGTH_SHORT).show();

        /*        }
                else {

                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
                }
            }
        });*/

        //click en likes
//MODI         holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
       /*     @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikedByActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
//MODI       });*/

        holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start DetallesPostActivity
                Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                context.startActivity(intent);
            }
        });


    }

    /*private void clickSharePost(ImageView pImageIv, String pTitle, String pDescription, String uid, String pId) {
        /*algunas publicaciones contienen solo texto, y algunas contienen imagen y texto, por lo que las manejaremos a ambas*/
        //obtener la imagen de la vista de imagen (imageview)
      /*  BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
        if (bitmapDrawable == null){

            //compartir SIN imagen
            shareTextOnly(pTitle, pDescription, uid, pId);

        }
        else {*/
/*
            //compartir CON imagen

            //convertir imagen a mapa de bits (convert image to bitmap)
            Bitmap bitmap = bitmapDrawable.getBitmap();
            shareImageAndText(pTitle, pDescription, bitmap,  uid, pId);

        }

    }*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void clickMoreBtn(String uid, ImageButton moreBtn, String pId, String pImage,ImageView pImageIv, String pTitle, String pDescription, ImageView saveIv) {
        //PRUEBA ESTADO
        //creando un men?? emergente que actualmente tiene la opci??n Eliminar, agregaremos m??s opciones m??s adelante
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);

        //mostrar la opci??n de eliminar solo en las publicaciones del usuario que ha iniciado sesi??n actualmente
        if (uid.equals(myUid)){
            //agregar elementos en el men??
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar publicaci??n");        //importante menu

            //Quitar o Agregar Post
            String imageno = "noImage";
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Posts");
            Query query1 = ref1.orderByChild("pId").equalTo(pId);
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull  DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()){
                                ModelPost modelPost = ds.getValue(ModelPost.class);

                                if (imageno.equals(modelPost.getpImage())) {
                                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicaci??n");
                                    editar=1;
                                }
                            }
                            if (editar!=1) {
                                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicaci??n");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull  DatabaseError error) {

                        }
                    });
            //Quitar o Agregar Post




            //Quitar o Agregar Post
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull  DataSnapshot snapshot) {
                            int a=0;
                            for (DataSnapshot ds: snapshot.getChildren()){

                                if (ds.exists()){
                                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Publicar nuevamente");
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
            //Quitar o Agregar Post}

            //Guardar o dejar de Guardar Post
            FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                    .child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(pId).exists()){
                        //SAVED
                        popupMenu.getMenu().add(Menu.NONE, 4, 0, "No guardar");
                    }
                    else {
                        //SAVE
                        popupMenu.getMenu().add(Menu.NONE, 4, 0, "Guardar");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //Guardar o dejar de Guardar Post

        }
        popupMenu.getMenu().add(Menu.NONE, 3, 0, "Ver detalles");
        //popupMenu.getMenu().add(Menu.NONE, 4, 0, "Guardar");
        popupMenu.getMenu().add(Menu.NONE, 5, 0, "Compartir");



        //elemento de escucha de clics
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                Toast.makeText(context, "a= "+editar, Toast.LENGTH_SHORT).show();
                if (id==0){
                    //se hace clic en eliminar
                    //beginDelete(pId, pImage);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("??Desea eliminar este articulo?")
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    beginDelete(pId, pImage);

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
                else if (id==1){

                    if (editar==0){
                        //se hace clic en Edit
                        //inicie AddPostActivity con la clave "editPost" y la identificaci??n de la publicaci??n en la que se hizo clic
                        Intent intent = new Intent(context, AddPostActivity.class);
                        intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                        intent.putExtra("editPostId", pId);      //utilizado en AddPostActivity
                        context.startActivity(intent);
                    }
                    else {
                        //se hace clic en Edit
                        //inicie AddPostActivity con la clave "editPost" y la identificaci??n de la publicaci??n en la que se hizo clic
                        Intent intent = new Intent(context, AddTextPostActivity.class);
                        intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                        intent.putExtra("editPostId", pId);      //utilizado en AddPostActivity
                        context.startActivity(intent);
                    }



                }
                else if (id==2){
                    //esatado post
                    //PRUEBA FUNCIONA
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                    int a=0;
                                    for (DataSnapshot ds: snapshot.getChildren()){

                                        if (ds.exists()){
                                            //Toast.makeText(context, "mostrar 1: " +a, Toast.LENGTH_SHORT).show();
                                            unBlockUser(pId);
                                            a = 1;
                                        }
                                    }
                                    //Toast.makeText(context, "mostrar 2" , Toast.LENGTH_SHORT).show();
                                    if (a != 1){

                                        blockUser(pId);
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull  DatabaseError error) {

                                }
                            });
                    //PRUEBA FUNCIONA
                }
                else if (id==3){
                    //start DetallesPostActivity
                    Intent intent = new Intent(context, DetallesPostActivity.class);
                    intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                    context.startActivity(intent);
                }
                else if (id==4){
                    //Guardar Post
                    if (saveIv.getTag().equals("save")){
                        FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                                .child(pId).setValue(true);
                    }
                    else {
                        FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                                .child(pId).removeValue();
                    }
                }
                else if(id==5){

                    /*algunas publicaciones contienen solo texto, y algunas contienen imagen y texto, por lo que las manejaremos a ambas*/
                    //obtener la imagen de la vista de imagen (imageview)
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                    if (bitmapDrawable == null){

                        //compartir SIN imagen
                        shareTextOnly(pTitle, pDescription, uid, pId);

                    }
                    else {

                        //compartir CON imagen

                        //convertir imagen a mapa de bits (convert image to bitmap)
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        shareImageAndText(pTitle, pDescription, bitmap,  uid, pId);

                    }
                }
                return false;
            }
        });
        //Muestrame el menu
        popupMenu.show();
        editar=0;
        //PRUEBA ESTADO

    }

    private void checkIsBlocked(String pId, MyHolder holder, int position) {
        //comprobar cada post, si est?? Reciclado o no
        //si el uid del usuario existe en "BlockedUsers", ese usuario est?? bloqueado, de lo contrario no

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int a=0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                holder.estadoIv.setImageResource(R.drawable.ic_estado_reciclado);
                                a = 1;
                            }
                        }
                        if (a!=1) {
                            holder.estadoIv.setImageResource(R.drawable.ic_estado_sinreciclar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsNoImage(String pId, MyHolder holder, int position, String pImagee){
        String imageno = "noImage";
        //comprobar cada post, si est?? Reciclado o no
        //si el uid del usuario existe en "BlockedUsers", ese usuario est?? bloqueado, de lo contrario no

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int a=0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){

                                if (imageno.equals(pImagee)) {
                                    holder.estadoIv.setVisibility(View.GONE);
                                    //holder.estadoIv.setImageResource(R.drawable.ic_blocked_red);
                                    //Toast.makeText(context, "cugggggggggggantos: ", Toast.LENGTH_SHORT).show();
                                }
                                //holder.estadoIv.setVisibility(View.INVISIBLE);

                                a = 1;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String pId) {
        //bloquear al usuario, agregando uid al nodo "RecyclerPosts" del usuario actual

        // poner valores en hasmap para poner en db
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").child(pId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //bloqueado con ??xito
                        Toast.makeText(context, "Reciclado con exito...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //no se pudo bloquear
                        Toast.makeText(context, "Fallo: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser(String pId) {
        //desbloquear al usuario, eliminando uid del nodo "RecyclerPosts" del usuario actual

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                //elimin?? los datos del post reciclado de la lista de usuarios reciclados del usuario actual
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Desbloqueo con exitoso
                                                Toast.makeText(context, "Puesto nuevamente...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //no se pudo desbloquear
                                                Toast.makeText(context, "Fallo: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void shareTextOnly(String pTitle, String pDescription, String uid, String pId) {
        //concatenar t??tulo y descripci??n para compartir
        String shareBody = pTitle +"\n"+ pDescription;

        //compartir intenci??n (intent)
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //en caso de que comparta a trav??s de una aplicaci??n de correo electr??nico
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //texto para compartir
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); //mensaje para mostrar en el cuadro de di??logo para compartir

        //No enviar notificacion a uno mismo
        /*if (uid.equals(myUid)){
            Toast.makeText(context, "Funcionn!", Toast.LENGTH_SHORT).show();
        }
        else {
            addToHisNotifications(""+uid, ""+pId, "Compartio tu publicaci??n");
        }*/
        //No enviar notificacion a uno mismo

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap, String uid, String pId) {
        //concatenar t??tulo y descripci??n para compartir
        String shareBody = pTitle +"\n"+ pDescription;

        //primero guardaremos esta imagen en la cach??, obtendremos la uri de la imagen guardada
        Uri uri = saveImageToShare(bitmap);

        //compartir intencion (intent)
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
        //copiar el mismo c??digo en PostDetailActivity

        //No enviar notificacion a uno mismo
        /*if (uid.equals(myUid)){
            Toast.makeText(context, "Funcionn!", Toast.LENGTH_SHORT).show();
        }
        else {
            addToHisNotifications(""+uid, ""+pId, "Compartio tu publicaci??n");
        }*/
        //No enviar notificacion a uno mismo


    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //crear si no existe
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.keneth.android.androidnatural.fileprovider",
                    file);
        }
        catch (Exception e)
        {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        return uri;
    }

    //likes el que es

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    //Al usuario le ha gustado esta publicaci??n.
                    /* Para indicar que a este usuario (SigneIn) le gusta la publicaci??n
                    Cambiar el icono dibujable de la izquierda del bot??n Me gusta
                    Cambiar el texto del bot??n Me gusta de "Me gusta" a "Me gust??"*/
                    //holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);//IMPORTANTE
                    holder.likeBtn.setImageResource(R.drawable.ic_meencanta_green);
                    //holder.likeBtn.setText("Me gusto");  //antes "Liked"
                }
                else {
                    //Al usuario NO le ha gustado esta publicaci??n.
                    /* Para indicar que a este usuario (SigneIn) NO le gusta la publicaci??n
                    Cambiar el icono dibujable de la izquierda del bot??n Me gusta
                    Cambiar el texto del bot??n Me gusta de "Me gusto" a "Me gusta"*/
                   //holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);//IMPORTANTE
                    holder.likeBtn.setImageResource(R.drawable.ic_meencanta_white);
                    //holder.likeBtn.setText("Me gusta");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreObtions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        //creando un men?? emergente que actualmente tiene la opci??n Eliminar, agregaremos m??s opciones m??s adelante
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //mostrar la opci??n de eliminar solo en las publicaciones del usuario que ha iniciado sesi??n actualmente
        if (uid.equals(myUid)){
            //agregar elementos en el men??
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar publicaci??n");        //importante menu
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicaci??n");
            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Articulo entregado");
        }
        popupMenu.getMenu().add(Menu.NONE, 3, 0, "Ver detalles");

        //elemento de escucha de clics
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    //se hace clic en eliminar
                    beginDelete(pId, pImage);
                }
                else if (id==1){
                    //se hace clic en Edit
                    //inicie AddPostActivity con la clave "editPost" y la identificaci??n de la publicaci??n en la que se hizo clic
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                    intent.putExtra("editPostId", pId);      //utilizado en AddPostActivity
                    context.startActivity(intent);
                }
                else if (id==2){
                    //esatado post
                    //estadoPost(uid, uName,uEmail,uLocation,uDp,pTimeStamp,pTitle,pDescription,pImage,pTimeStamp);
                    //uid, uName, uEmail, uLocation, uDp, 8 pTimeStamp, pTitle, pDescription, pImage, pTimeStamp
                    /*if (postList.get(position).getEstado()){
                        unBlockUser(pId); //=hisUID
                    }
                    else {
                        blockUser(pId);
                    }*/
                }
                else if (id==3){
                    //start DetallesPostActivity
                    Intent intent = new Intent(context, DetallesPostActivity.class);
                    intent.putExtra("postId", pId); //Obtendr?? detalles de la publicaci??n usando este id, su id de la publicaci??n que se hizo clic
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //Muestrame el menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //la publicaci??n puede ser con o sin imagen

        if (pImage.equals("noImage")){
            //la publicaci??n no tiene imagen
            deleteWithoutImage(pId);
        }
        else{
            //la publicaci??n es con imagen
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        //barra de progreso
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Eliminando...");

        /*Pasos:
        * 1) Eliminar imagen usando url
        * 2) Eliminar de la base de datos usando la identificaci??n de publicaci??n*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //imagen eliminada, ahora eliminada la base de datos

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue(); //eliminar valores de la firebase donde el pid coincide
                                    deletheReciclado(pId, pImage);
                                }
                                //eliminado
                                Toast.makeText(context, "Articulo eliminado", Toast.LENGTH_SHORT).show();
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
                        //fall??, no puedo ir m??s lejos
                        pd.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletheReciclado(String pId, String pImage) {

        //desbloquear al usuario, eliminando uid del nodo "RecyclerPosts" del usuario actual

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("RecyclerPosts").orderByChild("pId").equalTo(pId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                //elimin?? los datos del post reciclado de la lista de usuarios reciclados del usuario actual
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //no se pudo desbloquear
                                                Toast.makeText(context, "Fallo: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void deleteWithoutImage(String pId) {
        //barra de progreso
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Eliminando...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue(); //eliminar valores de la firebase donde el pid coincide
                }
                //eliminado
                Toast.makeText(context, "Articulo eliminado!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    @Override
    public int getItemCount() {
        return postList.size();
    }

    //ver clase de soporte
    class MyHolder extends RecyclerView.ViewHolder{

        //vistas desde row_post.xml
        ImageView uPictureIv, pImageIv, like, commentBtn, shareBtn,likeBtn, estadoIv,saveIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv,pCategoriaTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        //Button likeBtn;
                //commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //inicializamos los views
 //MODI            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
 //MODI           uNameTv = itemView.findViewById(R.id.uNameTv);
 //MODI           pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
//MODI            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
//MODI        pCategoriaTv = itemView.findViewById(R.id.pCategoriaTv);

            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            //shareBtn = itemView.findViewById(R.id.shareBtn);

            saveIv = itemView.findViewById(R.id.saveIv);
            estadoIv = itemView.findViewById(R.id.estadoIv);

            profileLayout = itemView.findViewById(R.id.profilelayout);

            //like = itemView.findViewById(R.id.like);

        }
    }

//prueba liked sinnombre2

    private void isLiked(String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")   //nombre de la tabla BD
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(firebaseUser.getUid()).exists()){

                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");

                }else{

                    imageView.setImageResource(R.drawable.ic_like_black);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void nrLikes(final TextView likes, String postid){   //funcion numero de likes

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes") //nombre tabla
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                likes.setText(dataSnapshot.getChildrenCount() + "");
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

    //prueba guardar Post sinnombre2
    private void isSaved(String postid, ImageView imageView){
        FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                }
                else {
                    imageView.setImageResource(R.drawable.ic_estado_sinreciclar);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //prueba guardar Post sinnombre2


}
