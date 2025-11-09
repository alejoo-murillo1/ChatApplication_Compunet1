import { Menu } from "../components/Menu.js";
import { GroupList } from "../components/GroupList.js";

import axios from "axios";

export class CreateGroupPage {
  constructor(router) {
    this.router = router;
    this.selectedUsers = new Set();
  }

  render() {
    const box = document.createElement("div");
    box.id = "box";
    const container = document.createElement("div");
    container.classList.add("container");

    const header = new Menu(this.router).render();

    const sidebar = new GroupList(this.router, () => {}).render();

    const content = document.createElement("div");
    content.classList.add("chat");

    content.innerHTML = `
      <div class="top-chat sidebar-text">Crear un nuevo grupo</div>
      <div class="create-group-form">
        <div class="group-name-section">
          <p class="form-label">Ingresa el nombre del grupo</p>
          <input id="groupName" class="font-text-input2" type="text" placeholder="Nombre del grupo">
        </div>

        <div class="user-selection-section">
          <p class="form-label">Selecciona los participantes</p>
          <div id="userList" class="user-circle-container">
            <p class="light-text">Cargando usuarios...</p>
          </div>
        </div>

        <button id="createGroupBtn" class="create-group-btn" disabled>Crear grupo</button>
      </div>
    `;

    container.append(sidebar, content);
    box.append(header, container);

    this.loadUsers(content);

    return box;
  }

  async loadUsers(content) {
    const userListDiv = content.querySelector("#userList");
    const users = await this.fetchUsersOnline();

    if (users.length === 0) {
      userListDiv.innerHTML = `<p class="light-text">No hay usuarios en línea disponibles</p>`;
    } else {
      userListDiv.innerHTML = users.map(u => `
        <div class="user-item">
          <div class="user-circle" data-username="${u}">
            ${u.charAt(0).toUpperCase()}
          </div>
          <span class="circle-name">${u}</span>
        </div>
      `).join("");
    }

    userListDiv.querySelectorAll(".user-circle").forEach(circle => {
      circle.addEventListener("click", () => {
        const username = circle.dataset.username;
        if (this.selectedUsers.has(username)) {
          this.selectedUsers.delete(username);
          circle.classList.remove("selected");
        } else {
          this.selectedUsers.add(username);
          circle.classList.add("selected");
        }

        const btn = content.querySelector("#createGroupBtn");
        btn.disabled = this.selectedUsers.size < 2;
      });
    });

    const createBtn = content.querySelector("#createGroupBtn");
    createBtn.addEventListener("click", async () => {
      const groupName = content.querySelector("#groupName").value.trim();
      if (!groupName) {
        alert("Por favor ingresa un nombre para el grupo");
        return;
      }

      const members = Array.from(this.selectedUsers);

      const currentUser = sessionStorage.getItem("username");
      if (currentUser && !members.includes(currentUser)) {
        members.push(currentUser);
      }

      const payload = { name: groupName, members };

      try {
        const response = await axios.post("http://localhost:3001/create-group", payload);
        console.log("Respuesta del proxy:", response.data);
        console.log("Grupo creado correctamente");
        this.router.navigateTo("/groups");
      } catch (error) {
        console.error("Error al crear grupo:", error);
      }
    });
  }

  async fetchUsersOnline() {
    try {
      const response = await axios.get("http://localhost:3001/users");
      if (response.data.status === "ok" || response.data.status === "warning") {
        const users = response.data.data?.users || response.data.body?.users || [];
        const currentUser = sessionStorage.getItem("username");
        return users.filter(u => u !== currentUser);
      }
      return [];
    } catch (error) {
      console.error("Error al obtener usuarios:", error);
      return [];
    }
  }
}
