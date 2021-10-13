package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.adapter.AdapterPosts;
import com.keneth.android.androidnatural.modelos.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    //views del xml
    ImageView avatarIv, coverIv,commentBtn;
    TextView nameTv, emailTv, phoneTv, locationTv;
    RecyclerView postsRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;
    String hisUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Perfil");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //iniciali8zamos los views
        avatarIv = findViewById(R.id.avatarIv);
        //coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        locationTv = findViewById(R.id.locationTv);
        postsRecyclerView = findViewById(R.id.recyclerview_posts);

        commentBtn = findViewById(R.id.commentBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        //obtener el uid del usuario en el que se hizo clic para recuperar sus publicaciones
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");//adapterusers


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //checkc hasta obtener los datos requeridos
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();
                    String location = ""+ ds.child("location").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    locationTv.setText(location);
                    try {
                        //si se recibe la imagen, establezca
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        //si hay alguna excepción al obtener la imagen, establezca el valor predeterminado
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }

                    try {
                        //si se recibe la imagen, establezca
                        //Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        //si hay alguna excepción al obtener la imagen, establezca el valor predeterminado
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //inBlockedORNot(hisUID);
                Intent intent = new Intent(ThereProfileActivity.this, ChatActivity.class);
                intent.putExtra("hisUid",uid);
                startActivity(intent);
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadHistPosts();


    }

    private void loadHistPosts() {
        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //mostrar la publicación más reciente primero, para esta carga desde la última                       IMPORTANTE
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview(establezca este diseño en vista de reciclaje)
        postsRecyclerView.setLayoutManager(layoutManager);

        //lista de publicaciones de inicio
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //(query)consulta para cargar publicaciones
        /*Siempre que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación.
         * por lo que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //obtener todos los datos de esta referencia
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //agregar a la lista
                    postList.add(myPosts);

                    //adaptador (adapter)
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchHistPosts(String searchQuery){
        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        //mostrar la publicación más reciente primero, para esta carga desde la última                       IMPORTANTE
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview(establezca este diseño en vista de reciclaje)
        postsRecyclerView.setLayoutManager(layoutManager);

        //lista de publicaciones de inicio
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //(query)consulta para cargar publicaciones
        /*Siempre que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación.
         * por lo que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //obtener todos los datos de esta referencia
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){                          //IMPORTANTE
                        //agregar a la lista
                        postList.add(myPosts);
                    }

                    //adaptador (adapter)
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        //Obtener usuario actua
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //El usuario ha iniciado sesión aquí
            //establecer el correo electrónico del usuario que inició sesión
            //  mProfileTv.setText(user.getEmail());

        }
        else{
            //el usuario no ha iniciado sesión, vaya a MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false); //ocultar añadir publicación de esta actividad

        //ocultar el iconOS de este fragmento
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_settings2).setVisible(false);
        menu.findItem(R.id.action_prueba).setVisible(false);
        menu.findItem(R.id.action_contraseña).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchview para buscar publicaciones específicas del usuario
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el botón de búsqueda
                if (!TextUtils.isEmpty(query)){
                    //search
                    searchHistPosts(query);
                }
                else {
                    loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //llamado cada vez que el usuario escribe cualquier letra
                if (!TextUtils.isEmpty(s)){
                    //search
                    searchHistPosts(s);
                }
                else {
                    loadHistPosts();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
