    package com.keneth.android.androidnatural;

    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.keneth.android.androidnatural.adapter.CommentAdapter;
    import com.keneth.android.androidnatural.modelos.Comment;
    import com.keneth.android.androidnatural.modelos.ModelUser;

    import java.util.HashMap;
    import java.util.List;

    public class ComentariosActivity1 extends AppCompatActivity {

        //VIDEO 10
        private RecyclerView recyclerView;
        private CommentAdapter commentAdapter;
        private List<Comment> commentList;
        //VIDEO 10

    EditText addcomment;
    ImageView image_profile;
    TextView post;

    String postid;
    String publisherid;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Opiniones");  //Titulo del Fragmento comentarios
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //opciones bonot
        Intent intent = getIntent();
        postid = intent.getStringExtra("postId");            //mio "pId"  / postId     //que es "postid"
        publisherid = intent.getStringExtra("publisherid");  //mio ""         //que es "publisherid"
        //opciones boton

        //VIDEO 10
        /*
        recyclerView = findViewById(R.id.recyclear_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postid);
        recyclerView.setAdapter(commentAdapter);

        *///VIDEO 10

        //VIDEO 9
        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        //VIDEO 9
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //VIDEO 9


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (addcomment.getText().toString().equals("")){

                    Toast.makeText(ComentariosActivity1.this, "No puedes enviar un comentario vacio!", Toast.LENGTH_SHORT).show();
                }else{

                    addComment();
                }
            }
        });

        getImage();

        //VIDEO 10
        // readComments();

        //VIDEO 10


    }
        //VIDEO 9
        private void addComment(){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);  //nombre de la tabla BD

            //boton de opciones
            String commentid = reference.push().getKey();
            //boton de opciones

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("comment", addcomment.getText().toString());         //el COMENTARIO
            hashMap.put("publisher", firebaseUser.getUid());                 //la Puvlicacion

            //boton de opciones
            hashMap.put("commentid", commentid);



            reference.child(commentid).setValue(hashMap);
            //boton de opciones

            //VIDEO 18
            // addNotifications();
            //VIDEO 18
            addcomment.setText("");

        }

        //VIDEO 18
        /*private void addNotifications(){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userid", firebaseUser.getUid());
            hashMap.put("text", "commented: "+addcomment.getText().toString());      //NO FUNCIONA
            hashMap.put("postid", postid);
            hashMap.put("ispost", true);

            reference.push().setValue(hashMap);
        }*/
        //VIDEO 18



        private void getImage(){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()); //mio

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ModelUser user = dataSnapshot.getValue(ModelUser.class);
                    Glide.with(getApplicationContext()).load(user.getImage()).into(image_profile);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        //VIDEO 9

        //VIDEO 10
    /*    private void readComments(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);//bien

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);

                }

                commentAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }
*/
        //VIDEO 10
    }