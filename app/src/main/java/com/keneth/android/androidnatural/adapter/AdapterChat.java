package com.keneth.android.androidnatural.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.R;
import com.keneth.android.androidnatural.modelos.ModelChat;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflar layouts: row_chat_left.xml para el receptor, row_chat_right.xml para el remitente
        if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        //obtener datos
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();

        //convertir la marca de tiempo a dd/mm/aa hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        final String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if (type.equals("text")){
            //mensaje de texto
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }
        else {
            //mensaje de imagen
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
        }

        //establecer datos
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(imageUrl).into(holder.profileIv);
        }
        catch (Exception e){

        }

        //haga clic para mostrar el cuadro de diálogo de eliminación
        holder.messageLAyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mostrar mensaje de borrado confirmar diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Eliminar");
                builder.setMessage("¿Estás seguro de eliminar este mensaje?");
                //boton eliminar
                builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DeleteMesaage(position);
                    }
                });
                //botton cancelar eliminacion
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //descartar diálogo
                        dialog.dismiss();
                    }
                });
                //crear y mostrar dialogo
                builder.create().show();
            }
        });

        //establecer el estado del mensaje visto / entregado
        if (position==chatList.size()-1){
            if (chatList.get(position).isSeen()){
                holder.isSeenTv.setText("Seen");
            }
            else {
                holder.isSeenTv.setText("Delivered");
            }
        }
        else {
            holder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void DeleteMesaage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*Lógica:
         * Obtener la marca de tiempo del mensaje en el que se hizo clic
         * Compare la marca de tiempo del mensaje en el que se hizo clic con todos los mensajes en Chats
         * Cuando ambos valores coincidan, eliminar el mensaje de chat
         * Esto permitirá al remitente eliminar su mensaje y el del destinatario.
         */
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    /*si desea permitir que el remitente elimine solo su mensaje, entonces
                    * comparar el valor del remitente con el uid del usuario actual
                    * si coinciden, significa que es el mensaje del remitente que está intentando eliminar*/
                    if (ds.child("sender").getValue().equals(myUID)){

                        /*Podemos hacer una de dos cosas aquí
                         * 1) Eliminar el mensaje de los chats
                         * 2) Establecer el valor del mensaje "Este mensaje fue eliminado ...
                         * Así que haz lo que quieras "*/

                        // 1) Eliminar el mensaje de Chats
                        ds.getRef().removeValue();

                        // 2) Establecer el valor del mensaje "Este mensaje fue eliminado..."//prueba esto
                        /*HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);*/

                        Toast.makeText(context, "menssage delete...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();
                        
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //obtener usuario actualmente registrado
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    //ver clase de soporte
    class MyHolder extends RecyclerView.ViewHolder{

        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLAyout; //para hacer clic en el oyente(Listener) para mostrar eliminar


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //inicializamos views
             profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
             messageTv = itemView.findViewById(R.id.messageTv);
             timeTv = itemView.findViewById(R.id.timeTv);
             isSeenTv = itemView.findViewById(R.id.isSeentTv);
            messageLAyout = itemView.findViewById(R.id.messageLayout);
        }
    }


}
