package com.keneth.android.androidnatural.notifications;

public class Token {

    /*Un token de FCM, o más comúnmente conocido como un token de registro.
    Una identificación emitida por el GCM servidores de conexión a la aplicación cliente que le permite recibir mensajes*/

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
