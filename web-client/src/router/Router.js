export class Router {
  constructor(root, routes) {
    this.root = root;
    this.routes = routes;
    window.onpopstate = () => this.load(window.location.pathname);
  }

  navigateTo(path) {
    history.pushState({}, "", path);
    this.load(path);
  }

  load(path) {
    const route = this.routes.find(r => r.path === path);
    if (route) {
      this.root.innerHTML = "";
      const page = new route.component(this);
      this.root.appendChild(page.render());
    }
  }
}
