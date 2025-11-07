import { Menu } from "../components/Menu.js";

export class CreateGroupPage {
  constructor(router) {
    this.router = router;
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("chat-area");

    const header = new Menu(this.router).render();
    div.append(header);

    const content = document.createElement("div");
    content.innerHTML = `
      <h2>Nuevo grupo</h2>
      <p>Selecciona los participantes:</p>
      <div id="users">
        ${["user_1","user_2","user_3","user_4","user_5","user_6","user_7"]
          .map(u => `<label><input type="checkbox" value="${u}"/> ${u}</label><br>`).join("")}
      </div>
      <input id="groupName" placeholder="Ingresa el nombre del grupo">
      <button id="createGroup">Crear grupo</button>
    `;
    content.querySelector("#createGroup").addEventListener("click", () => {
      this.router.navigateTo("/groups");
    });

    div.append(content);
    return div;
  }
}
