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
import com.keneth.android.androidnatural.adapter.AdapterPostSearch;
import com.keneth.android.androidnatural.adapter.AdapterPosts;
import com.keneth.android.androidnatural.modelos.ModelPost;

import java.util.ArrayList;
import java.util.List;

public class PostSearchActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String isUpdateKey;
    String uid;
    String ubicacion;
    CardView categorias,card;
    SearchView svSearch;
    ImageView atrasIv;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    //ADAPTER BUSCAR
    List<ModelPost> postList2;
    AdapterPostSearch adapterPostSearch;
    //ADAPTER BUSCAR

    //xd
    ProgressBar progressBar;
    //xd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);

        svSearch = findViewById(R.id.svBuscar);
        atrasIv = findViewById(R.id.atrasIv);

        //inicializacion firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();   //firebaseDatabase = FirebaseDatabase.getInstance();   //databaseReference = firebaseDatabase.getReference("Users");
        databaseReference = firebaseDatabase.getReference("Users");
        //vista recicladora y sus propiedades
        recyclerView = findViewById(R.id.postRecyclerview);

        //lista de publicaciones de inicio
        postList = new ArrayList<>();
        postList2 = new ArrayList<>();

        //xd
        progressBar = findViewById(R.id.progress_circular);
        //xd

        //Traer key(categoria)
        //obtener datos a través de la intención del adaptador de la actividad anterior

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
                for (DataSnapshot ds : snapshot.getChildren()){
                    String location = ""+ ds.child("location").getValue();
                    ubicacion = ""+ ds.child("location").getValue();
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
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el botón de búsqueda
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query, ubicacion);
                } else {
                    loadPosts(ubicacion);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //llamado cuando el usuario presiona cualquier letra
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText, ubicacion);
                } else {
                    loadPosts(ubicacion);
                }
                return false;
            }
        });

        atrasIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void loadPosts(String l) {
        //vista recicladora y sus propiedades
        recyclerView.setHasFixedSize(true);
        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);                                                //Normal
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);                                      //2 Columnas
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);   //2 columnas bien                       //2 Columnas
        //mostrar la publicación más reciente primero, para esta carga desde la última                       IMPORTANTE
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview(establezca este diseño en vista de reciclaje)
        recyclerView.setLayoutManager(layoutManager);


        String imageno = "noImage";
        //ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //obtener todos los datos de esta ref
        //(query)consulta para cargar publicaciones
        /*Siempre que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación.
         * por lo que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        //Toast.makeText(getContext(), "mi loca: "+l, Toast.LENGTH_SHORT).show();
        Query query = ref.orderByChild("uLocation").equalTo(l);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//1                postList.clear();
                postList2.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    //postList.add(modelPost);

                    //cargar solo los que tienen imagen
                    if (imageno.equals(modelPost.getpImage())) {
                        //return;
                    }
                    else{
//1                        postList.add(modelPost);
                       postList2.add(modelPost);
                    }
                    /*if (modelPost.getpTitle().toLowerCase().contains(modelPost.getuLocation().toLowerCase()) ){//                           AQUI
                        postList.add(modelPost);
                    }*/
                    /*if (modelPost.getUid() != null && modelPost.getuLocation().equals(chatlist.getId())){
                        postList.add(modelPost);
                        break;
                    }*/


                    //adapter
//1                    adapterPosts = new AdapterPosts(PostSearchActivity.this, postList); //postList
                    adapterPostSearch = new AdapterPostSearch(PostSearchActivity.this, postList2); //postList
                    //configurar el adaptador para reciclarview
//1                    recyclerView.setAdapter(adapterPosts);
                    recyclerView.setAdapter(adapterPostSearch);
                }

                //adapterPosts.notifyDataSetChanged(); //de sinnombre2                                             PONERLO CUANDO TENGAS DATOS

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
        if (user != null){
            //El usuario ha iniciado sesión aquí
            //establecer el correo electrónico del usuario que inició sesión
            //  mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        }
        else{
            //el usuario no ha iniciado sesión, vaya a MainActivity
            startActivity(new Intent(PostSearchActivity.this, MainActivity.class));
            finish();
        }
    }

    private void searchPosts(String searchQuery, String location) {
        //vista recicladora y sus propiedades
        recyclerView.setHasFixedSize(true);
        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);                                      //2 Columnas
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);   //2 columnas bien                       //2 Columnas

        //mostrar la publicación más reciente primero, para esta carga desde la última                       IMPORTANTE
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview(establezca este diseño en vista de reciclaje)
        recyclerView.setLayoutManager(layoutManager);

        String imageno = "noImage";
        //ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //obtener todos los datos de esta ref
        Query query = ref.orderByChild("uLocation").equalTo(location);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//1                postList.clear();
                postList2.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);


                    //modelPost.getuLocation().toLowerCase().contains(ubicacion.toLowerCase())
                    if (imageno.equals(modelPost.getpImage())) {
                        //return;
                    }
                    else{
                            if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||   //modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())
                                    modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())){//                           AQUI
//1                                postList.add(modelPost);
                                postList2.add(modelPost);
                            }

                    }


                    //adapter
//1                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    adapterPostSearch = new AdapterPostSearch(PostSearchActivity.this, postList2);
                    //configurar el adaptador para reciclarview
//1                    recyclerView.setAdapter(adapterPosts);
                    recyclerView.setAdapter(adapterPostSearch);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                Toast.makeText(PostSearchActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}