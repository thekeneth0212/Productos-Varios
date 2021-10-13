package com.keneth.android.androidnatural;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MiAplicacion extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        //habilitar firebase sin conexión
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //después de habilitarlo, los datos que se cargan también se podrán ver sin conexión
    }
}
