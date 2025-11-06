export class Router {
  constructor(rootElement, routes) {
    this.rootElement = rootElement;
    this.routes = routes;
    window.onpopstate = () => this.render(window.location.pathname);
  }

  navigateTo(path) {
    window.history.pushState({}, "", path);
    this.render(path);
  }

  render(path) {
    const PageClass = this.routes[path];
    if (!PageClass) {
      this.rootElement.innerHTML = "<h2>Página no encontrada</h2>";
      return;
    }
    const page = new PageClass(this);
    this.rootElement.innerHTML = "";
    this.rootElement.appendChild(page.render());
  }
}
