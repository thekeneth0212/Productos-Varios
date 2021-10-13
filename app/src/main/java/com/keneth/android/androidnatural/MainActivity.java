package com.keneth.android.androidnatural;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //views
    Button mRegisterbtn, mLoginbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializamos los views
        mRegisterbtn = findViewById(R.id.register_btn);
        mLoginbtn = findViewById(R.id.login_btn);

        //manejar botón de registro click
        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inicio RegisterActivity
                startActivity(new Intent(MainActivity.this, RegistrarActivity.class));
            }
        });
        //manejar botón de inicio de sesión clic
        mLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Iniciar LoginActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

}

    /*En esta parte (01) haremos lo siguiente
        01-Agregar permiso de Internet (requerido para firebase)
        02-Agregar botones de registro e inicio de sesión en MainActivity
        03-Crear actividad de registro
        04-Crea un proyecto de Firebase y conecta la aplicación con ese proyecto
        05-Verifique el archivo google-services.json para asegurarse de que la aplicación esté conectada con firebase
        06-Registro de usuario mediante correo electrónico y contraseña
        07-Crear perfilActividad
        08-Ir a la actividad del perfil después de registrarse / iniciar sesión
        
      *En la siguiente parte (02) con inicio de sesión futuro
        01- Hacer perfil Lanzador de actividades
        02- Al iniciar la aplicación Verifique si el usuario decidio permanecer en ProfileActivity; de lo contrario, vaya a MainActivity
        03- Crear Login Activity
        04-Después de iniciar sesión, vaya a ProfileActivity
        05- Agregar menú de opciones para agregar la opción de cierre de sesión
        02- Despues de cerrar sesion ir a MainActivity

       *En la siguiente parte (03) con inicio de sesión futuro
        01-Agregar la opción de contraseña de rocover en LoginActivity
        02- El registro de la contraseña olvidada mostrará un cuadro de diálogo.
        * Vista de texto (como etiqueta)
        * Edittext (correo electrónico de entrada)
        * Botón (enviará instrucciones de recuperación de contraseña) a su correo electrónico
        03- Pequeñas mejoras

        * En la siguiente parte (04)
        01-Agregar función de inicio de sesión de Google
        * Requisitos:
        * Habilite el inicio de sesión de Google desde Firebase Authentication
        * Agregar correo electrónico de soporte al proyecto
        * Agregar certificado SHAI
        * Agregar biblioteca de Google Signin

        * En la siguiente parte (05)
        01-Guardar la información del usuario registrado (nombre, correo electrónico, uid, teléfono, imagen)
        * en la base de datos en tiempo real de firbase
        * Requisitos:
        * i) Agregar Firebase Realtime Database
        * ii) Cambiar las reglas de la base de datos en tiempo real de Firebase
        02- Agregar botón de navegación en ProfileActivity que tiene tres menús
        * Yo casa
        * ii) Perfil (información del usuario como nombre, correo electrónico, uid, teléfono, imagen)
        * iii) Todos los usuarios
        //agregar menú en menu.xml para mostrar en la navegación del botón

        *en la parte (07)
        *->Editar perfil:
        *     1) Nombre
        *     2) Teléfono
        *     3) Perfil
        *     4) Foto de portada
        * -> Requisitos:
        *     1) Permisos de cámara y almacenamiento (para elegir una imagen de la cámara o galería)
        *     2) Bibliotecas de Firebase Storage (para cargar y recibir foto de perfil / portada)
        * -> Actualización de UI:
        *     1) Agregar ImageView (para la foto de portada)
        *     2) Agregue FloatinActionButton para mostrar el diálogo que contiene opciones para Editar perfil
        *     3) Agregar imagen predeterminada para la imagen de perfil

        En esta parte (08):
        * -> Mostrar usuarios de la base de datos de Firebase Realtime en RecyclerView
        * Mostraremos la siguiente información de cada usuario:
        * 1) Imagen de perfil: mostrar en CircularImageView [biblioteca]
        * 2) Nombre
        * 3) Correo electrónico
        *
        * Agregar biblioteca CardView
        * Agregar biblioteca RecyclearView
        * Agregar biblioteca CircularImageView
        *
        * Fila de diseño (row_user.xml) para RecyclerView

        En esta parte (09):
        Buscar usuario (no distingue entre mayúsculas y minúsculas) de la lista de usuarios
        1) Por nombre
        2) Por correo electrónico
        *
        * Primero agregue SearchView en menu_main.xml
        * Mover el menú de opciones de la actividad del panel a cada fragmento

        En esta parte (10):
        * -> Diseñar actividad de chat {Crear nueva actividad vacía}
        * La barra de herramientas contendrá el icono del receptor, el nombre y el estado como en línea / fuera de línea
        * -> Agregar nuevo fragmento para la lista de chat
        * Agregue este fragmento a BottonNavigation

        En esta parte (11):
        * -> Mostrar la imagen de perfil y el nombre de Receiver en la barra de herramientas
        * -> Enviar mensaje a cualquier usuario
        *  Primero agregue Firebase Auth con la opción de cierre de sesión en ChatActivity también

        En esta parte (12):
        * ->Mostrar mensajes enviados
        * -Diseñar diferentes diseños para emisor y receptor
        * -Utilizaré un fondo personalizado para el remitente y el receptor que puedes descargar usando
        *   link en la descripcion
        * -El diseño del receptor (a la izquierda) contendrá la imagen de perfil, el mensaje y la hora
        * -Sender Layout (a la derecha) contendrá el mensaje y la hora

         En esta parte (13):
        * ->Mostrar el estado En línea / Última visita del usuario en la barra de herramientas de la actividad del chat
        * Cuando el usuario se registre, agregue una clave más llamada onlineStatus que tenga valor en línea
        * Crear método que establecerá el valor en onliStatus en línea o Visto por última vez en ...
        * Llamar a este método está onStart con valor en línea
        * Llamar a este método está en pausa con valor como marca de tiempo
         * Obtenga el valor si está en línea, simplemente muestre; de ​​lo contrario, convierta la marca de tiempo a la hora

         En esta parte (14):
        * ->Estado de escritura del estado del usuario en la barra de herramientas de la actividad del chat
        * Cuando el usuario se registre, agregue una tecla más llamada typingTo tener valor uid del receptor
        * Agregar textChange oyente para editar texto
        * EditText no está vacío significa que el usuario está escribiendo algo
        * así que configure su valor como el uid del receptor
        * EditText vacío significa que el usuario no está escribiendo
            así que establezca su valor como noOne

         En esta parte (15):
        * ->Borrar mensaje
        * ¿Quién puede eliminar que?
        * 1) El remitente puede eliminar solo su propio mensaje O
        * 2) El remitente puede eliminar su mensaje y el del destinatario
        * Después de hacer clic en eliminar, sucederá?
        * 1) Eliminar mensaje O
        * 2) Actualizar el valor del mensaje a "Este mensaje fue eliminado ..."

        En esta parte (16):
        * ->Enviar notificación de mensaje (usando FCM (Firebase Cloud Messaging))
        * Cuando el usuario envía un mensaje, el receptor recibirá la notificación
        * que contiene el mensaje.
        * Cuando el usuario hace clic en la notificación, se abrirá el chat con ese
        * persona ".

        En esta parte (17):
        Publica una publicación en firebase.
        * La publicación contendrá nombre de usuario, correo electrónico, uid, dp, hora de publicación, descripción del título, imagen, etc.
        * El usuario puede publicar una publicación con y sin imagen
        * Crear AddPostActivity
        * Agregue otra opción en la barra de acción para ir a AddPostActivity
        * La imagen se puede importar desde la galería o tomarse de la cámara

        MIRAR IMAGEN AL ESCOGERLA, SE AGRANDA

        En esta parte (18):
        * ->Mostrar todas las publicaciones agregadas por todos los usuarios en HomeFragment
        * 1) Agregar recicladorView en HomeFragment
        * 2) Crear row.xml para recyclerView
        * 3) crear una clase de modelo para recyclerView
        * 4) crear una clase de adaptador para recyclerView
        * 5) obtener y calzar publicaciones en recyclerView

        En esta parte (19):
        * ->Mostrar publicaciones específicas del usuario
        * La publicación del usuario que ha iniciado sesión se mostrará en ProfileFragment
        * La publicación de otro usuario se mostrará en ThereProfileActivity
        *
        * Cambios en la lista de usuarios:
        * 1) Haga clic en cualquier usuario para mostrar un cuadro de diálogo con dos opciones
        * 1) Chat: vaya a la actividad de chat para conversar con esa persona.
        * 2) Perfil: Ver perfil de esa persona
        * Cambios en PostList
        * 1) Haga clic en la parte superior de cualquier publicación para mostrar el perfil del usuario de la publicación
        * Cambios en ChatActivity
        * 1) Ocultar el icono AddPost de la barra de herramientas

        En esta parte (20):
        * ->Eliminar publicaciones:
        * Crear menú emergente
        * Cuando el usuario hace clic en el botón de 3 puntos en cualquier publicación, se mostrará un menú emergente con la opción Eliminar
        * La opción de eliminar se mostrará solo a las publicaciones del usuario actual
        * Cuando se hace clic en el botón Eliminar, esa publicación se eliminará
         *
         * Lógica:
         * Compararemos el uid del usuario actual con el uid en las publicaciones.
         * Donde ambos uids coinciden, significa que la publicación es del usuario actual
         *
         * Lo haremos en AdapterPosts

         En esta parte (21):
        * ->Editar publicaciones:
        *  >Agregar la opción "Editar" en el menú emergente
        *> No crearemos otra actividad para editar la publicación, sino que usaremos AddPostActivity para editar la publicación.
        *> Al hacer clic en "Editar" se iniciará AddPostActivity con una clave "editPost".
        *> Siempre que AddPostActivity comience, verifica si tiene una cadena de intención adicional "editPost", luego haremos Editar
        * Publicar tarea de lo contrario Agregar nuevas tareas de publicación

        En esta parte (22):
        * ->lIKE a la publicacion:
        *  >Agregue la función "Me gusta Publicar" en esta aplicación
        *> Agregue otra clave (pLikes) en cada publicación, que contenga el número total de Me gusta
        *> Botón Agregar para la publicación que me gustó
        *  > ¿Cómo se verá la base de datos (de Me gusta)?
        *    -Likes
        *     -PostID
        *          UID (del usuario que inició sesión): Me gustó
        *          UID (del usuario que inició sesión): Me gustó
        *          UID (del usuario que inició sesión): Me gustó
        *       +PostID
        *       +PostID
        *       +PostID

        En esta parte (23):
        * ->Agregar comentario a cualquier publicación
        * -> Para ello abriremos el post en nueva actividad,
            donde la lista de comentarios y la opción de agregar comentario estarán disponibles

        En esta parte (24):
        * ->Mostrar comentarios
        * -> Utilizará recyclingView para mostrar comentarios
        * -> Cada comentario contendrá
        * Nombre del usuario (que publicó el comentario)
        * Imagen de perfil del usuario (que publicó el comentario)
        * El contenido real del comentario.

        En esta parte (25):
        * ->Eliminar comentarios
        * -> El usuario puede eliminar solo sus propios comentarios
        * Hemos guardado el UID del usuario (que agregó el comentario) en el comentario.
        * por lo que el UID de en el comentario es igual al UID del usuario actual, significa
        * este comentario es de un usuario que ha iniciado sesión actualmente, por lo que puede eliminar este comentario
        *   sobre esta base

        En esta parte (26):
        * ->Mostrar lista de chat
        *   Nombre de usuario
        *   Imagen de perfil de usuario
        *   Estado en línea / fuera de línea
        *   Ultimo mensaje

        En esta parte (27):
        * ->Compartir publicacion
        *   Requerirá la clase FileProvider para compartir imágenes
        *   Lets take intro of FileProvider on Androi's official docs
        *
        *   Creemos la carpeta "xml" y el archivo de recursos paths.xml para definir la ruta que se utilizará para compartir archivos.

        En esta parte (28):
        * ->Recodificar FCM (notificación de chat)
        * Nuestra biblioteca de mensajería de Firebase es demasiado antigua / depreciada
        * Lo actualizaremos a la última versión.
        * Utilizará volea en lugar de Retrofit
        * Migrar proyecto a AndroidX
        * Bibliotecas requeridas
        * 1) Volea
        * 2) Google gson

        En esta parte (29):
        * ->Resolver problema: nulo Me gusta nulo Comentarios
        * Causa: No hemos agregado (ningún valor para) "pLikes", "pComments" al agregar la publicación.
        * Solución: agregue un valor inicial de "pLikes" y "pComments" a "0".
        *veámoslo en captura de pantalla
        *
        * -> Enviar imagen / foto en el chat
        * Cambiar la fila de chat y la interfaz de usuario de ChatActivity
        * Verifique el permiso de almacenamiento / cámara
        * Elija la imagen de la cámara / galería
        * Subir a base de fuego
        * Agregue otro campo en ModelChat, es decir, "tipo"
        * Cuando el mensaje es texto, este "tipo" tendrá el valor "texto"
        * Cuando el mensaje es imagen, este "tipo" tendrá el valor "imagen"

        En esta parte (30):
        * ->Publicar notificaciones de publicaciones
        * -Utilizaremos Firebase Topic Messaging
        * -Crearemos una pantalla de configuración donde el usuario puede habilitar / deshabilitar las notificaciones de publicación
        * -Agregue el elemento de menú "Ajustes" en menu_main.xml para acceder a la Actividad de Ajustes
        * -Sólo estoy creando notificaciones para publicaciones, pero con este tutorial puedes implementar notificaciones para
        * Me gusta, comentarios, etc. también.
        * -> Requerir bibliotecas
        * -Volley                               (ya agregado para notificaciones de chat)
        * -Mensajería en la nube de Firebase    (ya agregado para notificaciones de chat)

        En esta parte (31) haremos lo siguiente
        01- Bloquear usuario
        02- Desbloquear usuario
        03- El usuario bloqueado no podrá enviar mensajes a quienes hayan bloqueado ese uso

        En esta parte (32) haremos lo siguiente
        01- Implementar capacidades sin conexión de Firebase
             Esta clase se utiliza principalmente para la inicialización del estado global antes de que se muestre la primera actividad.
             Pocos usos aceptables de una clase de aplicación personalizada:
             * Tarea especializada que debe ejecutarse antes de la creación de su primera actividad.
             * Inicialización global que debe compartirse entre todos los componentes (informes de fallas, persistencia).
             * Métodos estáticos para acceder fácilmente a datos inmutables estáticos, como un objeto cliente de red compartido.

            Dado que tenemos que agregar la función Firebase offline antes de usarla en cualquier lugar, eso lo estamos haciendo en una clase que se extiende con la clase Application.

        02- Recibir datos de otras aplicaciones, por ejemplo, si comparte texto o imágenes de otras aplicaciones, esta aplicación también podrá recibir y compartir esos datos dentro de la aplicación como Publicar.
        Así como una aplicación puede enviar datos a otras aplicaciones, también puede recibir datos de otras aplicaciones.

        Por ejemplo, cuando el usuario selecciona texto / imagen en otras aplicaciones y presiona el botón compartir, nuestra aplicación también se mostrará en la lista de aplicaciones.
        que puede recibir datos

        Recibiremos esos datos (texto o imagen) y los publicaremos como Publicación.

        En esta parte (33):
        * ->Agregue la pestaña Pantalla de notificaciones en el menú Botton. Todas las notificaciones como:
        * A alguien le gustó tu publicación
        * Alguien comentó tu publicación.

        También puedes hacerlo para algunas otras notificaciones como "Nueva publicación agregada", "Te envié un mensaje", etc. siguiendo este video.

        En esta parte (34):
        * ->Personas a las que les gustó la publicación
        * 1) Muestra la lista de usuarios a los que les gustó la publicación.
        *    Haga clic en el recuento de me gusta de una publicación para mostrar la lista de usuarios a los que les gustó esa publicación

        En esta parte (35):
        * ->Cambiar la contraseña del usuario*/


