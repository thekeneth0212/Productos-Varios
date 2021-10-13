package com.keneth.android.androidnatural.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.ChatActivity;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.ThereProfileActivity;
import com.keneth.android.androidnatural.modelos.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterLikes extends RecyclerView.Adapter<AdapterLikes.MyHoler>{
    Context context;
    List<ModelUser> userList;

    //para obtener el uid del usuario actual
    FirebaseAuth firebaseAuth;
    String myUid;
    //constructor
    public AdapterLikes(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_likes, parent, false);

        return new MyHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHoler holder, int position) {
        //obtener datos
        final String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        final String userEmail = userList.get(position).getEmail();

        //establecer datos
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_profile_black)
                    .into(holder.mAvatarIv);
        }
        catch (Exception e){
            //Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        //manejar elemento clic
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myUid.equals(hisUID) ){
                    //Toast.makeText(context, "Funciono", Toast.LENGTH_SHORT).show();

                }
                else {

                    //show dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(new String[]{"Perfil", "Mensaje"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which==0){
                                //clic en perfil
                                /*haga clic para ir a ThereProfileActivity con uid, este uid es del usuario que hizo clic
                                 * que se utilizará para mostrar datos / publicaciones específicos del usuario*/
                                Intent intent = new Intent(context, ThereProfileActivity.class);
                                intent.putExtra("uid",hisUID);
                                context.startActivity(intent);
                            }
                            if (which==1){
                                //clic en chat
                                /*Haga clic en el usuario de la lista de usuarios para comenzar a chatear / enviar mensajes
                                 * Inicie la actividad poniendo el UID del receptor
                                 * usaremos el UID del chat para identificar al usuario con el que vamos a chatear*/
                                //inBlockedORNot(hisUID);

                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("hisUid", hisUID);
                                context.startActivity(intent);

                            }
                        }
                    });
                    builder.create().show();
                }



            }
        });

    }

    private void inBlockedORNot(String hisUID){
        //Primero verifique si el remitente (usuario actual) está bloqueado por el receptor o no
        // lógica: si el uid del remitente (usuario actual) existe en "BlockedUsers" del receptor, el remitente (usuario actual) está bloqueado, de lo contrario no
        // si está bloqueado, solo muestra un mensaje, p. estás bloqueado por ese usuario, no puedes enviar mensaje
        // si no está bloqueado, simplemente inicie la actividad de chat

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context, "Estás bloqueado por ese usuario, no puedes enviar mensaje", Toast.LENGTH_SHORT).show();
                                //Bloqueado, no proceda más
                                return;
                            }
                        }
                        //no bloqueado, iniciar actividad
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }





    @Override
    public int getItemCount() {
        return userList.size();
    }
    //ver clase de soporte
    class MyHoler extends RecyclerView.ViewHolder{

        ImageView mAvatarIv;
        TextView mNameTv, mEmailTv;

        public MyHoler(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            //blockIv = itemView.findViewById(R.id.blockIv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }

}
