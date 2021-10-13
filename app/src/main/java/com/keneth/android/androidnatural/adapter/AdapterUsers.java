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

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    //para obtener el uid del usuario actual
    FirebaseAuth firebaseAuth;
    String myUid;
    //constructor
    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {
        //obtener datos
        final String hisUID = userList.get(i).getUid();  //uid del RECEPTOR
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        final String userEmail = userList.get(i).getEmail();

        //establecer datos
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_profile_black)
                    .into(holder.mAvatarIv);
        }
        catch (Exception e){
            //Toast.makeText(context, "Sin imagen: "+" "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        holder.blockIv.setImageResource(R.drawable.ic_nobloqueado);
        //compruebe si cada usuario esta bloqueado o no
        checkIsBlocked(hisUID, holder, i);


        //manejar elemento clic
        //ORIGINAL
        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 //ESTA BIEN
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Perfil", "Mensaje"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //clic en perfil
                            /*haga clic para ir a ThereProfileActivity con uid, este uid es del usuario que hizo clic
                             * que se utilizará para mostrar datos / publicaciones específicos del usuario*/
                       /*     Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);
                        }
                        if (which==1){
                            //clic en chat
                            /*Haga clic en el usuario de la lista de usuarios para comenzar a chatear / enviar mensajes
                             * Inicie la actividad poniendo el UID del receptor
                             * usaremos el UID del chat para identificar al usuario con el que vamos a chatear*/
                       /*     inBlockedORNot(hisUID);

                        }
                    }
                });
                builder.create().show();*/
                //ESTA BIEN

           /* }

        });*/
        //ORIGINAL


        //manejar elemento clic
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //PRUEBA FUNCIONAL

                //Primero verifique si el remitente (usuario actual) está bloqueado por el receptor o no
                // lógica: si el uid del remitente (usuario actual) existe en "BlockedUsers" del receptor, el remitente (usuario actual) está bloqueado, de lo contrario no
                // si está bloqueado, solo muestra un mensaje, p. estás bloqueado por ese usuario, no puedes enviar mensaje
                // si no está bloqueado, simplemente inicie la actividad de chat

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    if (ds.exists()){
                                        Toast.makeText(context, "BLOQUEASTE ESTE USUARIO, no puedes enviar mensaje", Toast.LENGTH_SHORT).show();
                                        //Bloqueado, no proceda más
                                        return;
                                    }
                                }

                                //ESTA BIEN
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
                                            inBlockedORNot(hisUID);

                                        }
                                    }
                                });
                                builder.create().show();
                                //ESTA BIEN
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //PRUEBA FUNCIONAL
            }

        });

        //haga clic para bloquear desbloquear usuario
        //ORIGINAL
        /*holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(i).isBlocked()){
                    unBlockUser(hisUID);
                }
                else {
                    blockUser(hisUID);
                }

            }
        });*/
        //ORIGINAL

        //PRUEBA FUNCIONA
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                int a=0;
                                for (DataSnapshot ds: snapshot.getChildren()){

                                    if (ds.exists()){
                                        //Toast.makeText(context, "mostrar 1: " +a, Toast.LENGTH_SHORT).show();
                                        unBlockUser(hisUID);
                                        a = 1;
                                    }
                                }
                                //Toast.makeText(context, "mostrar 2" , Toast.LENGTH_SHORT).show();
                                if (a != 1){

                                    blockUser(hisUID);
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull  DatabaseError error) {

                            }
                        });

            }
        });
        //PRUEBA FUNCIONA

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
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context, "Estás bloqueado por ese usuario, no puedes enviar mensaje", Toast.LENGTH_SHORT).show();
                                //Bloqueado, no proceda más
                                return;
                            }
                        }
                        //no bloqueado, iniciar actividad
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });

    }

    private void checkIsBlocked(String hisUID, MyHolder holder, int i) {
        //comprobar cada usuario, si está bloqueado o no
        //si el uid del usuario existe en "BlockedUsers", ese usuario está bloqueado, de lo contrario no
        int List;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                holder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                                //userList.get(i).setBlocked(true);

                                //Toast.makeText(context, "mostrar: "+ i , Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Que putas: "+error, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    //"bloqueo exitoso"
    private void blockUser(String hisUID) {
        //bloquear al usuario, agregando uid al nodo "BlockedUsers" del usuario actual

        // poner valores en hasmap para poner en db
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //bloqueado con éxito
                        Toast.makeText(context, "Bloqueado con éxito...", Toast.LENGTH_SHORT).show();
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

    //"desbloqueo exitoso"
    private void unBlockUser(String hisUID) {
        //desbloquear al usuario, eliminando uid del nodo "BlockedUsers" del usuario actual

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                //eliminó los datos de usuario bloqueados de la lista de usuarios bloqueados del usuario actual
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Desbloqueo con exitoso
                                                Toast.makeText(context, "Desbloqueo Exitoso...", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //no se pudo desbloquear
                                                Toast.makeText(context, "Fallo2: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    //funcion si lo bloquee a el
    private void tengoBlockedORNot(String hisUID){
        //Primero verifique si el remitente (usuario actual) está bloqueado por el receptor o no
        // lógica: si el uid del remitente (usuario actual) existe en "BlockedUsers" del receptor, el remitente (usuario actual) está bloqueado, de lo contrario no
        // si está bloqueado, solo muestra un mensaje, p. estás bloqueado por ese usuario, no puedes enviar mensaje
        // si no está bloqueado, simplemente inicie la actividad de chat

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
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
    //funcion si lo bloquee a el

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //ver clase de soporte
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv,blockIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            blockIv = itemView.findViewById(R.id.blockIv);
            mEmailTv = itemView.findViewById(R.id.emailTv);

        }
    }
}
