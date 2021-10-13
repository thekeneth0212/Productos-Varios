package com.keneth.android.androidnatural.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.TableroActivity;
import com.keneth.android.androidnatural.ThereProfileActivity;
import com.keneth.android.androidnatural.modelos.Comment;
import com.keneth.android.androidnatural.modelos.ModelComment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //enlazar row_comment.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //xd
        ModelComment comment1 = commentList.get(position);
        //xd

        //obtener los datos
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        //convertir la marca de tiempo a dd / mm / aaaa hh: mm am / pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //establecer los datos
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);
        //establecer dp de usuario
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_profile_black).into(holder.avatarIv);
        }
        catch (Exception e){

        }

        holder.avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myUid.equals(uid) ){
                    //Toast.makeText(context, "Funciono", Toast.LENGTH_SHORT).show();

                }
                else {
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //compruebe si este comentario es del usuario que ha iniciado sesión actualmente o no                          //IMPORTANTE
                if (myUid.equals(uid)){
                    //mi comentario
                    //mostrar cuadro de diálogo de eliminación
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Eliminar Comentario");
                    builder.setMessage("Esta seguro de eliminar este comentario?");
                    builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //eliminar comentario
                            deleteComment(cid);
                            //deleteWithoutImage(cid);
                        }
                    });
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //descartar diálogo
                            dialog.dismiss();
                        }
                    });
                    //mostrar diálogo
                    builder.create().show();
                }
                else {
                    //NO es mi comentario
                    Toast.makeText(context, "No puedes eliminar este comentario...", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        //comentario haga clic en oyente (comment click listener)
        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //compruebe si este comentario es del usuario que ha iniciado sesión actualmente o no                          //IMPORTANTE
                if (myUid.equals(uid)){
                    //mi comentario
                    //mostrar cuadro de diálogo de eliminación
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Eliminar Comentario");
                    builder.setMessage("Esta seguro de eliminar este comentario?");
                    builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //eliminar comentario
                            deleteComment(cid);
                            //deleteWithoutImage(cid);
                        }
                    });
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //descartar diálogo
                            dialog.dismiss();
                        }
                    });
                    //mostrar diálogo
                    builder.create().show();
                }
                else {
                    //NO es mi comentario
                    Toast.makeText(context, "No puedes eliminar este comentario...", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

    }

    private void deleteComment(String cid) {
        /*DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue(); //eliminará el comentario

        //sinnombre2
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments")
                                    .child(postId).child(cid)
                                    .removeValue();*/

        //eliminar comentario
        FirebaseDatabase.getInstance().getReference("Comments")
                .child(postId).child(cid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(context, "Eliminado!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //sinnombre2
        //ahora actualice el recuento de comentarios
        /*ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+ snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //declare views from row_comments.xml
        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
