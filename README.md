# ChatApplication_CNet
<h2>Curso: Computación en Internet 1 - Universidad Icesi</h2>
<h5>Santiago de Cali, Miércoles 15 de Octubre de 2025</h5>

<h3>Integrantes:</h3>
<ul>
  <li>Alejandro Murillo</li>
  <li>Isabella Candado</li>
  <li>Jesus Tascón</li>
</ul>

<h3><strong>Descripción:</strong></h3>
<p>
  <strong>ChatApplication</strong> es una aplicación de mensajería por consola. Incorpora funcionalidades como:
  <ul>
    <li><em>Registro</em> de Usuarios</li>
    <li><em>Registro</em> de Grupos</li>
    <li><em>Envío</em> de mensajes de texto a usuarios (Peer-to-Peer) y a Grupos de Chat</li>
    <li><em>Envío</em> de mensajes de voz a usuarios (Peer-to-Peer) y a Grupos de Chat</li>
    <li><em>Llamada</em> de voz en tiempo real a un Usuario o Grupo</li>
    <li><em>Almacenamiento/Persistencia</em> del historial de mensajes</li>
  </ul>

  Estas funciones se llevan a cabo por medio de comandos por teclado, estos se especifican a continuación:
</p>

<section>
  <h2>Comandos para el funcionamiento del Aplicativo</h2>
  <p>
    Para poder ejecutar la aplicación de forma óptima, debe ejecutarse el archivo <strong><em>ChatServer.java</em></strong>
    antes de iniciar las n-terminales del lado del cliente <strong><em>ChatApplication.java</em></strong>. En caso contrario, la app
    no podrá conectarse a ningún servidor, y su ejecución terminará.
  </p>

<p>
  Cuando la pantalla principal empieza su ejecución, le pedirá la dirección IP del Server,
  este es <em>Localhost</em> por defecto. Oprima <em>Enter</em> para continuar.
  
  Una vez completado el paso anterior, se desplegará el menú principal con 9 opciones:
  <ol>
    <li>Enviar mensaje de texto</li>
    <li>Enviar mensaje de voz</li>
    <li>Hacer una llamada</li>
    <li>Ver Historial</li>
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
      La lista de 7 opciones que le aparecerá es la siguiente:
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
