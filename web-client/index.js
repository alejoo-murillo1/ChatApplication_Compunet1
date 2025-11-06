import { Router } from "./src/router/Router.js";
import { HomePage } from "./src/pages/HomePage.js";
import { ChatPage } from "./src/pages/ChatPage.js";

// Registrar las rutas disponibles
const routes = {
  "/": HomePage,
  "/chat": ChatPage
};

// Inicializar router
const appElement = document.getElementById("app");
const router = new Router(appElement, routes);

// Iniciar en ruta raíz
router.navigateTo("/");

