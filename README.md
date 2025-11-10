# ChatApplication_Compunet1
#### Curso: Computación en Internet 1 - Universidad Icesi
<h5>Santiago de Cali, Miércoles 15 de Octubre de 2025</h5>

<h3>Integrantes:</h3>
<ul>
  <li>Isabella Candado</li>
  <li>Alejandro Murillo</li>
</ul>

<h3><strong>Descripción:</strong></h3>
<p>
  <strong>ChatApplication</strong> es una aplicación de mensajería web. Incorpora funcionalidades como:
  <ul>
    <li><em>Registro</em> de Usuarios</li>
    <li><em>Creación</em> de Grupos</li>
    <li><em>Envío</em> de mensajes de texto a usuarios (Peer-to-Peer) y a Grupos de Chat</li>
    <li><em>Almacenamiento/Persistencia</em> del historial de mensajes</li>
  </ul>

  Estas funciones están mapeadas en la aplicación por medio de diferentes páginas, las cuales se especifican a continuación.
</p>

<section>
  <h2>Guía para ubicarse dentro de la página</h2>
  <h3>Home Page</h3>
  <p>
    La página principal del chat te recibe con una bienvenida y una pequeña descripción. 
    En ella, verás un campo para ingresar el nombre de usuario con el que deseas entrar al chat.
    Luego de escribirlo, basta con presionar el botón de <em>Ingresar</em> para poder acceder a las funcionalidades del chat.
  </p>

<p>
  Cuando la pantalla principal empieza su ejecución, le pedirá la dirección IP del Server,
  este es <em>Localhost</em> por defecto. Oprima <em>Enter</em> para continuar.
  
  Una vez completado el paso anterior, se desplegará el menú principal con 10 opciones:
  <ol>
    <li>Enviar mensaje de texto</li>
    <li>Enviar mensaje de voz</li>
    <li>Hacer una llamada</li>
    <li>Ver Historial de Mensajes</li>
    <li>Ver Historial de Audios</li>
    <li>Escuchar Audios Pendientes</li>
    <li>Consultar mensajes de voz</li>
    <li>Responder llamada entrante</li>
    <li>Acceder al Menú de Grupos</li>
    <li>Salir</li>
  </ol>

  Ingrese el comando de su preferencia y siga las instrucciones presentadas en el aplicativo.
</p>
</section>

<section>
    <h2>Menú de Grupos</h2>
    <p>
      La lista de 8 opciones que le aparecerá es la siguiente:
      <ol>
        <li>Crear Grupo</li>
        <li>Agregar Miembro a unGrupo</li>
        <li>Enviar mensaje a un Grupo</li>
        <li>Enviar mensaje de voz a un Grupo</li>
        <li>Reproducir audios pendientes </li>
        <li>Hacer una llamada a un Grupo</li>
        <li>Responder una llamada de un Grupo</li>
        <li>Volver al Menú Principal</li>
      </ol>
  Ingrese el comando de su preferencia y siga las instrucciones presentadas en el aplicativo.
    </p>
  </section>

<section>
  <h2>Tecnologías Utilizadas</h2>
  <ul>
    <li><em>Lenguaje:</em> Java, JDK versión 25</li>
    <li><em>Persistencia:</em> Base de datos en PostgreSQL</li>
    <li><em>Interfaz de Sonido:</em> Librería <strong>Javax, módulo sound.sampled</strong></li>
    <li><em>Network: </em> Sockets <em>UDP y TCP</em> en el módulo <strong>java.net</strong></li>
    <li><em>Construcción/Manejo de Dependencias:</em> <strong>Gradle/Groovy</strong></li>
  </ul>
</section>
