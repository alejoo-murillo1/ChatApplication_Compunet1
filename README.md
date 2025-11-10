# ChatApplication - Compunet1
#### Curso: Computación en Internet 1 - Universidad Icesi

<small>Santiago de Cali, Miércoles 15 de Octubre de 2025</small>
### Integrantes:

- Isabella Candado
- Alejandro Murillo

## Tabla de Contenidos


  - [Descripción](#descripción)
  - [Configuración de Puertos](#configuración-de-puertos)
- [Guía para ejecutar el proyecto](#guía-para-ejecutar-el-proyecto)
  - [Requisitos](#requisitos)
  - [Paquetes de debes instalar](#paquetes-de-debes-instalar)
    - [Cliente web (Node.js)](#cliente-web-nodejs)
    - [Proxy (Node.js)](#proxy-nodejs)
  - [Comandos para ejecutar](#comandos-para-ejecutar)
    - [Server](#server)
    - [Proxy](#proxy)
    - [Web-client](#web-client)
- [Guía para ubicarse dentro de la página](#guía-para-ubicarse-dentro-de-la-página)
  - [Home Page](#home-page)
  - [Chat Page](#chat-page)
  - [Group Page](#group-page)
  - [Create Group Page](#create-group-page)


## Descripción:

**ChatApplication** es una aplicación de mensajería web. Incorpora funcionalidades como:

- Registro de Usuarios.
- Creación de Grupos.
- Envío de mensajes de texto a Usuarios o a Grupos.
- Almacenamiento/Persistencia del historial de mensajes de los chats.

> Estas funciones están mapeadas en la aplicación por medio de diferentes páginas, las cuales se especifican más adelante en **Guía para ubicarse dentro de la página**.

## Configuración de Puertos

Cada componente del proyecto se ejecuta en un puerto distinto en **localhost**:

| Componente      | Puerto | Descripción                                                       |
| --------------- | ------ | ----------------------------------------------------------------- |
| **Cliente Web** | `3000` | Aplicación web del usuario final, servida por Webpack Dev Server. |
| **Proxy**       | `3001` | Puente entre el cliente web y el servidor Java.                   |
| **Servidor**    | `5000` | Backend principal del sistema, desarrollado en Java con Gradle.   |

> **Notas:**
> - Asegúrate de que los puertos no estén ocupados por otros procesos.
> - Si necesitas modificarlos, puedes hacerlo:
>   - En el cliente, desde `webpack.config.js` (propiedad `devServer.port`).
>   - En el proxy, editando el archivo `proxy/Services.js`.
>   - En el servidor, ajustando el puerto configurado en la clase `Server.java`.

<br>

# Guía para ejecutar el proyecto

## Requisitos

Antes de ejecutar el proyecto, asegúrate de tener instalado lo siguiente:

- [Node.js](https://nodejs.org/) v22.14.0 o superior
- [Java JDK](https://adoptium.net/) 21 o superior
- [Gradle](https://gradle.org/) 9.0.0 o superior

## Paquetes de debes instalar

### Cliente web (Node.js)

Desde la raíz del proyecto, instala las dependencias necesarias:

```
npm i --save-dev webpack webpack-cli
npm i --save-dev webpack-dev-server
npm i --serve-dev html-webpack-plugin
npm i --save-dev style-loader css-loader
npm i axios
```

### Proxy (Node.js)

Desde la raíz del proyecto, instala las dependencias necesarias:

```bash
cd proxy
npm i express cors axios
```

## Comandos para ejecutar

### Server

En una terminal limpia sobre la raíz del proyecto, ejecuta la siguiente línea de comandos:

```bash
cd server
gradle clean build 
java -jar build/libs/server.jar
```

Para confirmar que se ejecutó correctamente, debes ver en consola:
`Server running on port: 5000`

### Proxy

En una terminal limpia sobre la raíz del proyecto, ejecuta la siguiente línea de comandos:

```bash
cd proxy
node Services.js
```

Para confirmar que se ejecutó correctamente, debes ver en consola:
`Proxy HTTP escuchando en http://localhost:3001`

### Web-client

En una terminal limpia sobre la raíz del proyecto, ejecuta la siguiente línea de comandos:

```bash
npx webpack serve
```

Para abrir la página web, deberás ir al enlace que sale tras ejecutar el comando.

```bash
[webpack-dev-server] Project is running at:
<i> [webpack-dev-server] Loopback: http://localhost:3000/
```


# Guía para ubicarse dentro de la página

## Home Page

La página principal del chat te recibe con una bienvenida y una pequeña descripción. 
En ella, verás un campo para ingresar el nombre de usuario con el que deseas entrar al chat. 
Luego de escribirlo, basta con presionar el botón de `Ingresar` para poder acceder a las funcionalidades del chat.

## Chat Page

Tan pronto ingreses, verás esta página. Puedes darte cuenta que estás en ella porque en el menú superior está resaltada la opción `Chat`.

Bajo el menú, a la izquierda, verás la lista de usuarios que están conectados, con los que podrás mensajear al presionar su nombre. 

> *Nota: Si no ves ninguno, es porque eres el único usuario conectado.*

En caso de haber hablado con ese usuario antes, podrás ver el historial de mensajes con ese usuario.

Para enviar un mensaje debes escribir el texto que deseas en la barra de texto al inferior de la pantalla y luego presionar el botón `Enviar`. 

## Group Page

Para ir a esta página, debes presionar la opción `Grupos` del menú. Puedes darte cuenta que estás en ella porque en el menú superior está resaltada la opción `Grupos`.

Bajo el menú, a la izquierda, verás la lista de grupos a los que perteneces, con los que podrás enviarles mensajes a todos los miembros al presionar el nombre del grupo. 

> *Nota: Si no ves ninguno, es porque no eres miembro de ningún grupo aún. **Te animamos a crear uno.***

En caso de haber hablado con ese grupo antes, podrás ver el historial de mensajes del grupo. Arriba de cada mensaje, podrás ver el emisor del mismo.

Para enviar un mensaje debes escribir el texto que deseas en la barra de texto al inferior de la pantalla y luego presionar el botón `Enviar`.

## Create Group Page

Para ir a esta página, debes presionar la opción `Nuevo grupo` que se encuentra en la parte inferior de la barra lateral de la página `Grupos`. Puedes darte cuenta que estás en ella porque en pantalla se ve **Crear un nuevo grupo**.

Verás un pequeño formulario, en el que debes ingresar el nombre del grupo y seleccionar, de los usuarios conectados, a quiénes quieres  incluir en el grupo. Para crear un grupo, **necesitas mínimo 3 usuarios (incluyéndote)**.

Luego de que la información esté lista, debes presionar el botón `Crear grupo` al final del formulario.
Luego se te dirigirá automáticamente a la página de Grupos para que puedas enviarle un mensaje a alguno.