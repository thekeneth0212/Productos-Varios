package com.keneth.android.androidnatural.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

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
import com.keneth.android.androidnatural.AddPostActivity;
import com.keneth.android.androidnatural.AddTextPostActivity;
import com.keneth.android.androidnatural.ComentariosActivity2;
import com.keneth.android.androidnatural.ComentariosPostTextActivity;
import com.keneth.android.androidnatural.PostLikedByActivity;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.ThereProfileActivity;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPostsText extends RecyclerView.Adapter<AdapterPostsText.MyHolder>{

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

    public AdapterPostsText(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");        //IMPORTANTE
        postsRef = FirebaseDatabase.getInstance().getReference().child("PostsText");

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflar diseño row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts2, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPostsText.MyHolder holder, int position) {

        //xd
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//
        final ModelPost post = postList.get(position);//


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
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
          holder.pTitleTv.setText(pTitle);
          holder.pDescriptionTv.setText(pDescription);
//MODI        holder.pCategoriaTv.setText(pCatecory);
          holder.pLikesTv.setText(pLikes); //p.ej. 100 me gusta
        //holder.pCommentsTv.setText(pComments+ " Comentarios"); //p.ej. 100 me gusta
        //establecer Me gusta para cada publicación
        setLikes(holder, pId);


        nrLikes(holder.pLikesTv, post.getpId());

        pd = new ProgressDialog(context);

        getComments(post.getpId(), holder.pCommentsTv);

        //establecer dp de usuario
        try {
           Picasso.get().load(uDp).placeholder(R.drawable.ic_profile_black).into(holder.uPictureIv);

        }
        catch (Exception e){

        }

        //manejar Botón clic
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                clickMoreBtn(uid,holder.moreBtn, pId, pImage,holder.pImageIv, pTitle,pDescription); //holder.pImageIv,pTitle, pDescription, uid, pId

            }
        });

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
                //start DetallesPostActivity
                //el que es
               /*Intent intent = new Intent(context, DetallesPostActivity.class);
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                context.startActivity(intent);*/
                //el que es
                //sinnombre2
                Intent intent = new Intent(context, ComentariosPostTextActivity.class);    //ese (pId) es = post.getPostid()
                intent.putExtra("postId", pId); //Obtendrá detalles de la publicación usando este id, su id de la publicación que se hizo clic
                intent.putExtra("publisherid", pImage);   //ese (pImage) es = post.getPublisher()
                context.startActivity(intent);

                /*Intent intent = new Intent(mContext, ComentariosActivity.class);
                intent.putExtra("postid", post.getPostid());           //nose
                intent.putExtra("publisherid", post.getPublisher());   //nose
                mContext.startActivity(intent);*/
                //sinnombre2

            }
        });

        holder.uPictureIv.setOnClickListener(new View.OnClickListener() { //cambie uPictureIv por profileLayout
            @Override
           public void onClick(View v) {
                /*haga clic para ir a ThereProfileActivity con uid, este uid es del usuario que hizo clic
                 * que se utilizará para mostrar datos / publicaciones específicos del usuario*/
           if (myUid.equals(uid) ){
               //es mi perfil, asi que no va a ir
               }
                else {

                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
                }
            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void clickMoreBtn(String uid, ImageButton moreBtn, String pId, String pImage, ImageView pImageIv, String pTitle, String pDescription) {
        //PRUEBA ESTADO
        //creando un menú emergente que actualmente tiene la opción Eliminar, agregaremos más opciones más adelante
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);

        //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
        if (uid.equals(myUid)){
            //agregar elementos en el menú
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar publicación");        //importante menu
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar publicación");


        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Compartir");



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
                    builder.setMessage("¿Desea eliminar este articulo?")
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
                        //inicie AddPostActivity con la clave "editPost" y la identificación de la publicación en la que se hizo clic
                        Intent intent = new Intent(context, AddTextPostActivity.class);
                        intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                        intent.putExtra("editPostId", pId);      //utilizado en AddPostActivity
                        context.startActivity(intent);
                    }
                    else {
                        //se hace clic en Edit
                        //inicie AddPostActivity con la clave "editPost" y la identificación de la publicación en la que se hizo clic
                        Intent intent = new Intent(context, AddTextPostActivity.class);
                        intent.putExtra("key", "editPost");//utilizado en AddPostActivity
                        intent.putExtra("editPostId", pId);      //utilizado en AddPostActivity
                        context.startActivity(intent);
                    }

                }
                else if(id==2){
                        //compartir SIN imagen
                        shareTextOnly(pTitle, pDescription, uid, pId);

                }
                return false;
            }
        });
        //Muestrame el menu
        popupMenu.show();
        editar=0;
        //PRUEBA ESTADO

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
        //concatenar título y descripción para compartir
        String shareBody = pTitle +"\n"+ pDescription;

        //compartir intención (intent)
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //en caso de que comparta a través de una aplicación de correo electrónico
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //texto para compartir
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); //mensaje para mostrar en el cuadro de diálogo para compartir

        //No enviar notificacion a uno mismo
        /*if (uid.equals(myUid)){
            Toast.makeText(context, "Funcionn!", Toast.LENGTH_SHORT).show();
        }
        else {
            addToHisNotifications(""+uid, ""+pId, "Compartio tu publicación");
        }*/
        //No enviar notificacion a uno mismo

    }

    //likes el que es
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

    private void beginDelete(String pId, String pImage) {
        //la publicación puede ser con o sin imagen
            //la publicación no tiene imagen
            deleteWithoutImage(pId);
    }

    private void deleteWithoutImage(String pId) {
        //barra de progreso
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Eliminando...");

        Query fquery = FirebaseDatabase.getInstance().getReference("PostsText").orderByChild("pId").equalTo(pId);
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

    class MyHolder extends RecyclerView.ViewHolder{

        //vistas desde row_post.xml
        ImageView uPictureIv, pImageIv, like, commentBtn, shareBtn,likeBtn, estadoIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv,pCategoriaTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        //Button likeBtn;
        //commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //inicializamos los views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
//MODI        pCategoriaTv = itemView.findViewById(R.id.pCategoriaTv);

            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);


            profileLayout = itemView.findViewById(R.id.profilelayout);


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
