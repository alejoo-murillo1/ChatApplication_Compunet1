import axios from "axios";

export class Menu {
  constructor(router) {
    this.router = router;
  }

  render() {
    const header = document.createElement("header");
    const name = sessionStorage.getItem("username") || "Invitado";
    header.innerHTML = `
      <div id="menu-buttons">
        <button class="menu-text" id="chats">Chats</button>
        <button class="menu-text" id="groups">Grupos</button>
        <button class="menu-text" id="logout">Salir</button>
      </div>
      <div class="menu-text">Bienvenid@, ${name}</div>
    `;

    header.querySelector("#chats").onclick = () => this.router.navigateTo("/chat");
    header.querySelector("#groups").onclick = () => this.router.navigateTo("/groups");
    header.querySelector("#logout").onclick = () => {
      sessionStorage.clear();
      this.router.navigateTo("/");
    };

    const chatsBtn = header.querySelector("#chats");
    const groupsBtn = header.querySelector("#groups");
    const logoutBtn = header.querySelector("#logout");

    chatsBtn.addEventListener("click", () => this.router.navigateTo("/chat"));
    groupsBtn.addEventListener("click", () => this.router.navigateTo("/groups"));
    logoutBtn.addEventListener("click", async () => {
      const username = sessionStorage.getItem("username");

      if (username) {
        await this.updateUserStatusToOffline(username);
      }

      sessionStorage.clear();
      this.router.navigateTo("/");
    });

    

    // --- aplicar color activo según la ruta actual ---
    const currentPath = window.location.pathname; // obtiene la ruta actual

    if (currentPath === "/chat") {
      chatsBtn.classList.add("active");
    } else if (currentPath === "/groups" || currentPath === "/create-group") {
      groupsBtn.classList.add("active");
    }

    return header;
  }

  async updateUserStatusToOffline(username) {
    try {
      const userData = {
        name: username,
        status: "offline"
      };

      const response = await axios.put("http://localhost:3001/users/status", userData);

      console.log("Estado actualizado:", response.data);
    } catch (error) {
      console.error("Error al actualizar estado del usuario:", error);
    }
  }

}