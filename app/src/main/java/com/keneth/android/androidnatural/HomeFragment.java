package com.keneth.android.androidnatural;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
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

import static com.google.firebase.storage.FirebaseStorage.getInstance;


public class HomeFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    //firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String uid;
    String ubicacion;
    CardView categorias,foroCv,card;
    ImageView buscarIv,add_postIv;

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

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño de este fragmento
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        card = view.findViewById(R.id.card);
        foroCv = view.findViewById(R.id.foroCv);
        categorias = view.findViewById(R.id.categorias);
        buscarIv = view.findViewById(R.id.buscarIv);
        add_postIv = view.findViewById(R.id.add_postIv);


        //inicializacion firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();   //firebaseDatabase = FirebaseDatabase.getInstance();   //databaseReference = firebaseDatabase.getReference("Users");
        databaseReference = firebaseDatabase.getReference("Users");
        //vista recicladora y sus propiedades
        recyclerView = view.findViewById(R.id.postRecyclerview);



        //lista de publicaciones de inicio
        postList = new ArrayList<>();
        postList2 = new ArrayList<>();

        //xd
        progressBar = view.findViewById(R.id.progress_circular);
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

        categorias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opcionBoton(categorias);
            }

        });

        foroCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //inicie ForoActivity
                Intent intent = new Intent(getContext(), ForoActivity.class);
                startActivity(intent);
            }

        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "quee", Toast.LENGTH_SHORT).show();
            }
        });

        buscarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostSearchActivity.class);
                startActivity(intent);
            }
        });
        add_postIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ir a AddPost activity
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });

        return view;
    }

    private void opcionBoton(CardView categorias) {

        //showMoreObtions(holder.moreBtn, uid, myUid, pId, pImage);
        final String[] category = new String[1];

        //PRUEBA ESTADO
        //creando un menú emergente que actualmente tiene la opción Eliminar, agregaremos más opciones más adelante
        PopupMenu popupMenu = new PopupMenu(getContext(),categorias);

        //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
        //if (uid.equals(myUid)){
        //agregar elementos en el menú
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Material");        //importante menu
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Ciudad");
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Sin reciclar");

        //elemento de escucha de clics
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){

                    PopupMenu popupMenu = new PopupMenu(getContext(),categorias);

                    //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
                    //if (uid.equals(myUid)){
                    //agregar elementos en el menú
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Plastico");        //importante menu
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Madera");
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Metal");
                    popupMenu.getMenu().add(Menu.NONE, 3, 0, "Escombro");
                    popupMenu.getMenu().add(Menu.NONE, 4, 0, "Caucho");

                    //elemento de escucha de clics
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id==0){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CategoryActivity.class);
                                intent.putExtra("key", "Plastico");
                                startActivity(intent);
                            }
                            else if (id==1){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CategoryActivity.class);
                                intent.putExtra("key", "Madera");
                                startActivity(intent);
                            }
                            else if (id==2){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CategoryActivity.class);
                                intent.putExtra("key", "Metal");
                                startActivity(intent);
                            }
                            else if (id==3){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CategoryActivity.class);
                                intent.putExtra("key", "Escombro");
                                startActivity(intent);

                            }
                            else if (id==4){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CategoryActivity.class);
                                intent.putExtra("key", "Caucho");
                                startActivity(intent);
                            }
                            return false;
                        }
                    });
                    //Muestrame el menu
                    popupMenu.show();


                }
                else if (id==1){

                    PopupMenu popupMenu = new PopupMenu(getContext(),categorias);

                    //mostrar la opción de eliminar solo en las publicaciones del usuario que ha iniciado sesión actualmente
                    //if (uid.equals(myUid)){
                    //agregar elementos en el menú
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Bogota");        //importante menu
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Barranquilla");
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Cundinamarca");
                    popupMenu.getMenu().add(Menu.NONE, 3, 0, "Cali");
                    popupMenu.getMenu().add(Menu.NONE, 4, 0, "Pasto");

                    //elemento de escucha de clics
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id==0){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CiudadesActivity.class);
                                intent.putExtra("key", "Bogota");
                                startActivity(intent);
                            }
                            else if (id==1){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CiudadesActivity.class);
                                intent.putExtra("key", "Barranquilla");
                                startActivity(intent);
                            }
                            else if (id==2){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CiudadesActivity.class);
                                intent.putExtra("key", "Cundinamarca");
                                startActivity(intent);
                            }
                            else if (id==3){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CiudadesActivity.class);
                                intent.putExtra("key", "Cali");
                                startActivity(intent);

                            }
                            else if (id==4){
                                //inicie CategoryActivity con la clave "Plastico"
                                Intent intent = new Intent(getContext(), CiudadesActivity.class);
                                intent.putExtra("key", "Pasto");
                                startActivity(intent);
                            }
                            return false;
                        }
                    });
                    //Muestrame el menu
                    popupMenu.show();

                }
                else if (id==2){
                    //inicie CategoryActivity con la clave "Plastico"
                    Intent intent = new Intent(getContext(), SinReciclarActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });
        //Muestrame el menu
        popupMenu.show();
        //PRUEBA ESTADO
    }

    private void searchCategory(String s) {

        //ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //obtener todos los datos de esta ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);


                    if (modelPost.getpCategory().toLowerCase().contains(s.toLowerCase()) ||   //modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())
                            modelPost.getuLocation().toLowerCase().contains(s.toLowerCase())){//                           AQUI
                        postList.add(modelPost);
                    }


                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //configurar el adaptador para reciclarview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPosts(String l) {
        //vista recicladora y sus propiedades
        recyclerView.setHasFixedSize(true);
        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());                                                //Normal
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);                                      //2 Columnas
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);   //2 columnas bien                       //2 Columnas
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
                postList.clear();
//2                postList2.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    //postList.add(modelPost);

                    //cargar solo los que tienen imagen
                    if (imageno.equals(modelPost.getpImage())) {
                        //return;
                    }
                    else{
                        postList.add(modelPost);
//2                       postList2.add(modelPost);
                    }
                    /*if (modelPost.getpTitle().toLowerCase().contains(modelPost.getuLocation().toLowerCase()) ){//                           AQUI
                        postList.add(modelPost);
                    }*/
                    /*if (modelPost.getUid() != null && modelPost.getuLocation().equals(chatlist.getId())){
                        postList.add(modelPost);
                        break;
                    }*/


                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList); //postList
//2                    adapterPostSearch = new adapterPostSearch(getActivity(), postList2); //postList
                    //configurar el adaptador para reciclarview
                    recyclerView.setAdapter(adapterPosts);
//2                    recyclerView.setAdapter(adapterPostSearch);
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

    private void searchPosts(String searchQuery, String location){
        //vista recicladora y sus propiedades
        recyclerView.setHasFixedSize(true);
        //linear layout for recyclerview(diseño lineal para reciclador)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,true);         //Horizontal
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);                                      //2 Columnas
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
                postList.clear();
 //               postList2.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    //modelPost.getuLocation().toLowerCase().contains(ubicacion.toLowerCase())
                    if (imageno.equals(modelPost.getpImage())) {
                        //return;
                    }
                    else{
                        //if (ubicacion.equals(modelPost.getuLocation())){
                            if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||   //modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())
                                    modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getpCategory().toLowerCase().contains(searchQuery.toLowerCase())){//                           AQUI
                                postList.add(modelPost);
 //                               postList2.add(modelPost);
                            }
                        //}
                    }
                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
//                    adapterPostSearch = new AdapterPostSearch(getActivity(), postList2);
                    //configurar el adaptador para reciclarview
                    recyclerView.setAdapter(adapterPosts);
//                    recyclerView.setAdapter(adapterPostSearch);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkUserStatus(){
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
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//para mostrar la opción de menú en un fragmento
        super.onCreate(savedInstanceState);
    }

    /*menú de opciones de inflar*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //ocultar el icono de addpost de este fragmento
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_settings2).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_prueba).setVisible(false);
        menu.findItem(R.id.action_contraseña).setVisible(false);

        //searchview para buscar publicaciones por título / descripción de la publicación
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //escucha de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el botón de búsqueda
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query,ubicacion);
                }
                else {
                    loadPosts(ubicacion);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //llamado cuando el usuario presiona cualquier letra
                if (!TextUtils.isEmpty(s)){
                    searchPosts(s,ubicacion);
                }
                else {
                    loadPosts(ubicacion);
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /*manejar los clics en los elementos del menú*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener ID de item
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if (id==R.id.action_settings){
            //ir a ajustes activity
            startActivity(new Intent(getActivity(), AjustesActivity2.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }
}
