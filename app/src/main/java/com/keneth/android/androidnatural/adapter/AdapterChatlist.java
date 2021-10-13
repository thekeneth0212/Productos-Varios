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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.ChatActivity;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.ThereProfileActivity;
import com.keneth.android.androidnatural.modelos.ModelChatlist;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.keneth.android.androidnatural.modelos.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUser> userList; //obtener información de usuario
    private HashMap<String, String> lastMessageMap;

    //xd
    private FirebaseUser firebaseUser;
    //xd

    //Bloqueo
    FirebaseAuth firebaseAuth;
    String myUid;
    //Bloqueo

    //constructor
    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();

        //Bloqueo
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
        //Bloqueo
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflar el diseño row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //obtener datos
        String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        //establecer datos
        holder.nameTv.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        /*try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_profile_black).into(holder.profileIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_profile_black).into(holder.profileIv);
        }*/

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//
        final ModelUser user = userList.get(position);                                                  //PUESTO 09/06/2021
        Glide.with(context).load(user.getImage())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile_black)) //VIDEO 20
                .into(holder.profileIv);

        //set online status of other users in chatlist
        if (userList.get(position).getOnlineStatus().equals("online")){
            //En linea
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            //fuera de linea
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        //manejar el clic del usuario en la lista de chat
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //PRUEBA DE BLOQUEO
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int a=0;
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    if (ds.exists()){
                                        a = 1;
                                        Toast.makeText(context, "BLOQUEASTE ESTE USUARIO, no puedes enviar mensaje", Toast.LENGTH_SHORT).show();
                                        //Bloqueado, no proceda más
                                        return;
                                    }
                                }

                                if (a != 1) {
                                    //ESTA BIEN
                                    //iniciar la actividad de chat con ese usuario
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("hisUid", hisUid);
                                    context.startActivity(intent);
                                    //ESTA BIEN
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //PRUEBA DE BLOQUEO

                //iniciar la actividad de chat con ese usuario
                /*Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);*/
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size(); //tamaño de la lista
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //vistas de row_chatlist.xml
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //inicializamos las vistas
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
