package com.keneth.android.androidnatural;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keneth.android.androidnatural.adapter.AdapterUsers;
import com.keneth.android.androidnatural.modelos.ModelUser;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsuariosFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    SearchView searchSv;

    //firebase auth
    FirebaseAuth firebaseAuth;

    public UsuariosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);

        //inicializacion
        firebaseAuth = FirebaseAuth.getInstance();

        //searchSv = view.findViewById(R.id.searchSv);
        //inicializamos el recyclerView
        recyclerView = view.findViewById(R.id.users_recyclerView);
        //establecer sus propiedades
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //inicializamos la lista de usuarios
        userList = new ArrayList<>();

        //Consigue TODOS los usuarios
        getAllUsers();

        return view;
    }

    private void getAllUsers() { //igual a readUsers
        //Obtener usuario actual
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //obtener la ruta de la base de datos denominada "Users" que contiene información de los usuarios
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //obtener todos los datos de la ruta
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //obtener todos los usuarios excepto el usuario actualmente registrado
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    //adaptador
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //configurar el adaptador a la vista de reciclador
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(final String query) { //igual a search_bar
        //Obtener usuario actual
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //obtener la ruta de la base de datos denominada "Users" que contiene información de los usuarios
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //obtener todos los datos de la ruta
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    /*Condiciones para cumplir la búsqueda:
                     * 1) Usuario no usuario actual
                     * 2) El nombre de usuario o correo electrónico contiene texto ingresado en SearchView (no distingue entre mayúsculas y minúsculas) */

                    //obtener todos los usuarios buscados excepto el usuario actualmente registrado
                    if (!modelUser.getUid().equals(fUser.getUid())){

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                  modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }

                    }

                    //adaptador
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //actualizar adaptador
                    adapterUsers.notifyDataSetChanged();
                    //configurar el adaptador a la vista de reciclador
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_settings2).setVisible(false);
        menu.findItem(R.id.action_prueba).setVisible(false);
        menu.findItem(R.id.action_contraseña).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);



        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //escucha de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona el botón de búsqueda desde el teclado
                //si la consulta de búsqueda no está vacía, busque
                if (!TextUtils.isEmpty(query.trim())){
                    //el texto de búsqueda contiene texto, búscalo
                    searchUsers(query);
                }
                else{
                    //Buscar texto vacío, obtener todos los usuarios
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //llamado siempre que el usuario presione cualquier letra
                if (!TextUtils.isEmpty(query.trim())){
                    //el texto de búsqueda contiene texto, búscalo
                    searchUsers(query);
                }
                else{
                    //Buscar texto vacío, obtener todos los usuarios
                    getAllUsers();
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
        else if (id==R.id.action_settings){
            //ir a ajustes activity
            startActivity(new Intent(getActivity(), AjustesActivity2.class));
        }

        return super.onOptionsItemSelected(item);
    }

}


