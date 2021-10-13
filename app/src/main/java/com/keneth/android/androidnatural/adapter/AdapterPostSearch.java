package com.keneth.android.androidnatural.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.ComentariosActivity2;
import com.keneth.android.androidnatural.DetallesPostActivity;
import com.keneth.android.androidnatural.PostLikedByActivity;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPostSearch extends RecyclerView.Adapter<AdapterPostSearch.MyHolder> {

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

    public AdapterPostSearch(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");        //IMPORTANTE
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        //inflar diseño row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_postsearch, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPostSearch.MyHolder holder, int position) {
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
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
//MODI        holder.pCategoriaTv.setText(pCatecory);
        holder.pLikesTv.setText(pLikes); //p.ej. 100 me gusta
        //holder.pCommentsTv.setText(pComments+ " Comentarios"); //p.ej. 100 me gusta
        //establecer Me gusta para cada publicación
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
        checkIsNoImage(pId, holder, position,pImage);
        //No image


        //manejar Botón clic
        /*holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                clickMoreBtn(uid,holder.moreBtn, pId, pImage,holder.pImageIv, pTitle,pDescription); //holder.pImageIv,pTitle, pDescription, uid, pId

            }
        });*/

        //likes el que es
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

                Intent intent = new Intent(context, ComentariosActivity2.class);    //ese (pId) es = post.getPostid()
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                intent.putExtra("publisherid", pImage);   //ese (pImage) es = post.getPublisher()
                context.startActivity(intent);

            }
        });
//MODI        holder.pCommentsTv.setOnClickListener(new View.OnClickListener() {
           /* @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ComentariosActivity2.class);    //ese (pId) es = post.getPostid()
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
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
                 * que se utilizará para mostrar datos / publicaciones específicos del usuario*/
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

        /*holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start DetallesPostActivity
                Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                context.startActivity(intent);
            }
        });*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start DetallesPostActivity
                Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                context.startActivity(intent);
            }
        });

    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
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

    private void checkIsNoImage(String pId, MyHolder holder, int position, String pImagee) {
        String imageno = "noImage";
        //comprobar cada post, si está Reciclado o no
        //si el uid del usuario existe en "BlockedUsers", ese usuario está bloqueado, de lo contrario no

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

    private void getComments(String postid, TextView comments) {

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

    private void nrLikes(TextView likes, String postid) {
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

    private void setLikes(MyHolder holder, String pId) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(pId).hasChild(myUid)){
                    //Al usuario le ha gustado esta publicación.
                    /* Para indicar que a este usuario (SigneIn) le gusta la publicación
                    Cambiar el icono dibujable de la izquierda del botón Me gusta
                    Cambiar el texto del botón Me gusta de "Me gusta" a "Me gustó"*/
                    //holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);//IMPORTANTE
                    holder.likeBtn.setImageResource(R.drawable.ic_meencanta_green);
                    //holder.likeBtn.setText("Me gusto");  //antes "Liked"
                }
                else {
                    //Al usuario NO le ha gustado esta publicación.
                    /* Para indicar que a este usuario (SigneIn) NO le gusta la publicación
                    Cambiar el icono dibujable de la izquierda del botón Me gusta
                    Cambiar el texto del botón Me gusta de "Me gusto" a "Me gusta"*/
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

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //vistas desde row_post.xml
        ImageView uPictureIv, pImageIv, like, commentBtn, shareBtn,likeBtn, estadoIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv,pCategoriaTv, pLikesTv, pCommentsTv;
        //ImageButton moreBtn;
        //Button likeBtn;
        //commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull  View itemView) {
            super(itemView);

            //inicializamos los views
            //MODI            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            //MODI           uNameTv = itemView.findViewById(R.id.uNameTv);
            //MODI           pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
//MODI        pCategoriaTv = itemView.findViewById(R.id.pCategoriaTv);

            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            //moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            //shareBtn = itemView.findViewById(R.id.shareBtn);

            estadoIv = itemView.findViewById(R.id.estadoIv);

            profileLayout = itemView.findViewById(R.id.profilelayout);

            //like = itemView.findViewById(R.id.like);
        }
    }

}
