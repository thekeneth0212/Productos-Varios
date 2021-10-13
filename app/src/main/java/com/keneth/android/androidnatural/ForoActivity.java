package com.keneth.android.androidnatural;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.keneth.android.androidnatural.adapter.AdapterPostsText;
import com.keneth.android.androidnatural.modelos.ModelPost;

import java.util.ArrayList;
import java.util.List;

public class ForoActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    String cate;
    String uid;
    String ubicacion;
    CardView categorias, card;
    TextView titulo;
    SearchView svSearch;
    ImageView atrasIv,add;

    RecyclerView recyclerView;
    List<ModelPost> postList2;
    AdapterPostsText adapterPostsText;

    //xd
    ProgressBar progressBar;
    //xd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foro);

        add = findViewById(R.id.add);
        atrasIv = findViewById(R.id.atrasIv);
        card = findViewById(R.id.card);
        titulo = findViewById(R.id.texto);
        //svSearch = findViewById(R.id.svBuscar);

        svSearch = (SearchView) findViewById(R.id.svBuscar);
        /*ImageView icon = svSearch.findViewById(R.id.search_button);
        icon.setColorFilter(Color.WHITE);*/

        //inicializacion firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();   //firebaseDatabase = FirebaseDatabase.getInstance();   //databaseReference = firebaseDatabase.getReference("Users");
        databaseReference = firebaseDatabase.getReference("Users");
        //vista recicladora y sus propiedades
        recyclerView = findViewById(R.id.postRecyclerview);

        recyclerView.setHasFixedSize(true); //se lo puse de sinnombre2

        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);                                                //Normal
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);                                      //2 Columnas
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);   //2 columnas bien                       //2 Columnas
        //mostrar la publicación más reciente primero, para esta carga desde la última                       IMPORTANTE
        //set this layout to recyclerview(establezca este diseño en vista de reciclaje)
        //mostrar la publicación más reciente primero, para esta carga desde la última
        layoutManager.setStackFromEnd(true);
        gridLayoutManager.setReverseLayout(true);


        //establecer diseño(layout) en recyclerview
        recyclerView.setLayoutManager(gridLayoutManager);

        //lista de publicaciones de inicio
        postList2 = new ArrayList<>();

        //xd
        progressBar = findViewById(R.id.progress_circular);
        //xd


        //CARGAR DATOS
         /*tenemos que obtener información del usuario actualmente registrado, podemos obtenerlo usando el correo electrónico o uid del usuario
          Voy a recuperar los detalles del usuario usando el correo electrónico * /
        / * Al usar la consulta orderByChild mostraremos el detalle de un nodo
          cuya clave denominada correo electrónico tiene un valor igual al correo electrónico registrado actualmente.
          Buscará todos los nodos, donde la clave coincida obtendrá su detalle*/

        Query query = databaseReference.orderByChild("uid").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String location = "" + ds.child("location").getValue();
                    ubicacion = "" + ds.child("location").getValue();
                    loadPosts(ubicacion);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //CARGAR DATOS


        //Toast.makeText(getContext(), "ubica: "+location, Toast.LENGTH_SHORT).show();
        checkUserStatus();

        ///Toast.makeText(this, "texto search:" +svSearch, Toast.LENGTH_SHORT).show();

        //add posttext
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //inicie ForoActivity
                Intent intent = new Intent(ForoActivity.this, AddTextPostActivity.class);
                startActivity(intent);
            }
        });

        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el botón de búsqueda
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                } else {
                    loadPosts(ubicacion);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //llamado cuando el usuario presiona cualquier letra
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText);
                } else {
                    loadPosts(ubicacion);
                }
                return false;
            }
        });

        //Atras
        atrasIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForoActivity.super.onBackPressed();
            }
        });
    }


    private void loadPosts(String location) {
        //ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PostsText");
        //obtener todos los datos de esta ref
        Query query = ref.orderByChild("uLocation").equalTo(location);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList2.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                        postList2.add(modelPost);

                    //adapter
                    adapterPostsText = new AdapterPostsText(ForoActivity.this, postList2);
                    //configurar el adaptador para reciclarview
                    recyclerView.setAdapter(adapterPostsText);
                }

                //adapterPosts2.notifyDataSetChanged(); //de sinnombre2                                             PONERLO CUANDO TENGAS DATOS

                //xd
                progressBar.setVisibility(View.GONE);
                //xd
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                //Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();                         //QUITE 24/05/2021
            }
        });
    }

    private void checkUserStatus() {
        //Obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //El usuario ha iniciado sesión aquí
            //establecer el correo electrónico del usuario que inició sesión
            //  mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        } else {
            //el usuario no ha iniciado sesión, vaya a MainActivity
            startActivity(new Intent(ForoActivity.this, MainActivity.class));
            finish();
        }
    }

    private void searchPosts(String searchQuery) {

        //ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PostsText");
        //obtener todos los datos de esta ref
        Query query = ref.orderByChild("uLocation").equalTo(ubicacion);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList2.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);



                        if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||   //modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())
                                modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())) {//                           AQUI
                            postList2.add(modelPost);
                        }


                    //adapter
                    adapterPostsText = new AdapterPostsText(ForoActivity.this, postList2);
                    //configurar el adaptador para reciclarview
                    recyclerView.setAdapter(adapterPostsText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                Toast.makeText(ForoActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }





}