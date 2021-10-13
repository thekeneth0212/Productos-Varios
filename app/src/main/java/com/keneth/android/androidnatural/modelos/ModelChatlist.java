package com.keneth.android.androidnatural.modelos;

public class ModelChatlist {
    String id; //necesitaremos esta identificación para obtener la lista de chat, uid del remitente / receptor

    public ModelChatlist(String id) {
        this.id = id;
    }

    public ModelChatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
