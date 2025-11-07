import "./index.css";

import { Router } from "./src/router/Router.js";
import { routes } from "./src/router/Routes.js";

document.addEventListener("DOMContentLoaded", () => {
  const app = document.getElementById("app");
  const router = new Router(app, routes);
  router.navigateTo("/");
});
