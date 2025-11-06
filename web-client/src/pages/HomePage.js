export class HomePage {
  constructor(router) {
    this.router = router;
  }

  render() {
    const container = document.createElement("div");
    container.classList.add("chat-container");

    const title = document.createElement("h2");
    title.textContent = "Bienvenida al Chat Web";

    const btn = document.createElement("button");
    btn.textContent = "Entrar al chat";
    btn.style.margin = "20px";
    btn.onclick = () => this.router.navigateTo("/chat");

    container.style.justifyContent = "center";
    container.style.alignItems = "center";
    container.style.display = "flex";
    container.style.flexDirection = "column";

    container.appendChild(title);
    container.appendChild(btn);
    return container;
  }
}
