package com.keneth.android.androidnatural.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.keneth.android.androidnatural.AddPostActivity;
import com.keneth.android.androidnatural.AddTextPostActivity;
import com.keneth.android.androidnatural.ComentariosActivity2;
import com.keneth.android.androidnatural.DetallesPostActivity;
import com.keneth.android.androidnatural.PostLikedByActivity;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterMyFotos extends RecyclerView.Adapter<AdapterMyFotos.MyHolder>{


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

    public AdapterMyFotos(Context context, List<ModelPost> postList) {
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
        //inflar diseño row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_fotos, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMyFotos.MyHolder holder, int position) {

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
        String pLikes = postList.get(position).getpLikes(); //contiene el número total de me gusta para un post
        String pComments = postList.get(position).getpComments(); //contiene el número total de comentarios para un post

        String uLocation = postList.get(position).getuLocation();


        //convertir la marca de tiempo a dd / mm / aaaa hh: mm am / pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //establecer datos
//MODI        holder.uNameTv.setText(uName);
//MODI        holder.pTimeTv.setText(pTime);
//MODI        holder.pTitleTv.setText(pTitle);
//MODI        holder.pDescriptionTv.setText(pDescription);
//MODI        holder.pCategoriaTv.setText(pCatecory);
//MODI        holder.pLikesTv.setText(pLikes); //p.ej. 100 me gusta
        //holder.pCommentsTv.setText(pComments+ " Comentarios"); //p.ej. 100 me gusta
        //establecer Me gusta para cada publicación
//MODI        setLikes(holder, pId);

        //pruena likes sin nomrebe2
        //isLiked(post.getpId(), holder.like);
//MODI        nrLikes(holder.pLikesTv, post.getpId());
        //pruena likes sin nomrebe2

        pd = new ProgressDialog(context);

        //sinnombre
        //getComments(pId, holder.pCommentsTv);
        //getComments(post.getPostid(), holder.comments);
//modi        getComments(post.getpId(), holder.pCommentsTv);
        //sinnombrer

        //establecer dp de usuario
        try {
//MODI            Picasso.get().load(uDp).placeholder(R.drawable.ic_profile_black).into(holder.uPictureIv);

        }
        catch (Exception e){

        }

        //establecer imagen de publicación
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




        //likes el que es
/*
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obtener el número total de Me gusta de la publicación, cuyo botón Me gusta hizo clic
                //si el usuario que ha iniciado sesión actualmente no le ha gustado antes
                //aumenta el valor en 1, de lo contrario disminuye el valor en 1
                int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                //obtener la identificación de la publicación en la que se hizo clic
                String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike){
                            if (snapshot.child(postIde).hasChild(myUid)){
                                //ya NO me gustó, así que elimine me gusta
                                //postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));  //IMPORTANTE
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else {
                                //no me gustó, me gusta
                                //postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked"); //establecer cualquier valor
                                mProcessLike = false;

                                //No enviar notificacion a uno mismo
                                if (uid.equals(myUid)){
                                    //Toast.makeText(context, "Funciona2! ", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    addToHisNotifications("" + uid, "" + pId, "Le gusto tu publicación");
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



        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start DetallesPostActivity
                //el que es
               /*Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                context.startActivity(intent);*/
                //el que es
                //sinnombre2
 /*               Intent intent = new Intent(context, ComentariosActivity2.class);    //ese (pId) es = post.getPostid()
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                intent.putExtra("publisherid", pImage);   //ese (pImage) es = post.getPublisher()
                context.startActivity(intent);
*/
                /*Intent intent = new Intent(mContext, ComentariosActivity.class);
                intent.putExtra("postid", post.getPostid());           //nose
                intent.putExtra("publisherid", post.getPublisher());   //nose
                mContext.startActivity(intent);*/
                //sinnombre2
/*
            }
        });

*/
        holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start DetallesPostActivity
                Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                context.startActivity(intent);
            }
        });
    }

    private void checkIsBlocked(String pId, MyHolder holder, int position) {
        //comprobar cada post, si está Reciclado o no
        //si el uid del usuario existe en "BlockedUsers", ese usuario está bloqueado, de lo contrario no

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



    //likes el que es
/*
    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    //Al usuario le ha gustado esta publicación.
                    /* Para indicar que a este usuario (SigneIn) le gusta la publicación
                    Cambiar el icono dibujable de la izquierda del botón Me gusta
                    Cambiar el texto del botón Me gusta de "Me gusta" a "Me gustó"*/
                    //holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);//IMPORTANTE
 /*                   holder.likeBtn.setImageResource(R.drawable.ic_meencanta_green);
                    //holder.likeBtn.setText("Me gusto");  //antes "Liked"
                }
                else {
                    //Al usuario NO le ha gustado esta publicación.
                    /* Para indicar que a este usuario (SigneIn) NO le gusta la publicación
                    Cambiar el icono dibujable de la izquierda del botón Me gusta
                    Cambiar el texto del botón Me gusta de "Me gusto" a "Me gusta"*/
                    //holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);//IMPORTANTE
 /*                   holder.likeBtn.setImageResource(R.drawable.ic_meencanta_white);
                    //holder.likeBtn.setText("Me gusta");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

*/


    @Override
    public int getItemCount() {
        return postList.size();
    }

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
           likeBtn = itemView.findViewById(R.id.likeBtn);
           commentBtn = itemView.findViewById(R.id.commentBtn);
           //shareBtn = itemView.findViewById(R.id.shareBtn);
           estadoIv = itemView.findViewById(R.id.estadoIv);

           profileLayout = itemView.findViewById(R.id.profilelayout);

           //like = itemView.findViewById(R.id.like);
       }
   }


//prueba liked sinnombre2
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

}
