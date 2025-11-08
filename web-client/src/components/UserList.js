import axios from "axios";

export class UserList {
  constructor(router, onUserSelected) {
    this.router = router;
    this.onUserSelected = onUserSelected;
  }

  async fetchUsers() {
  try {
    const response = await axios.get("http://localhost:3001/users");
    return response.data;

  } catch (error) {
    console.error("Error al obtener usuarios:", error);
    return { status: "error", data: { message: "Error al conectar con el servidor" } };
  }
}

  render() {
    const div = document.createElement("div");
    div.classList.add("sidebar");

    div.innerHTML = `
      <h3 class="sidebar-text">Usuarios conectados</h3>
      <p class="light-text">Cargando usuarios...</p>
    `;

    this.fetchUsers().then(response => {
      const { status, data } = response;

      let content = "";
      
      if (status === "ok") {
        const users = data.users || [];
        const user = sessionStorage.getItem("username") || "Invitado";
        const otherUsers = users.filter(u => u !== user);
        
        content = otherUsers.map(u => `<div class="user" data-username="${u}">${u}</div>`).join("");
      } 
      else if (status === "warning") {
        content = `<p class="light-text">No hay más usuarios conectados</p>`;
      } 
      else {
        content = `<p class="light-text">Ocurrió un error al obtener los usuarios</p>`;
      }

      div.innerHTML = `
        <h3 class="sidebar-text">Usuarios conectados</h3>
        <div class="user-list">
          ${content}
        </div>
        <p class="light-text">Selecciona un usuario para enviarle un mensaje</p>
      `;

      // Escucha de clicks en los usuarios
      div.querySelectorAll(".user").forEach(el => {
        el.addEventListener("click", () => {
          // Quitar el estado activo anterior
          div.querySelectorAll(".user").forEach(u => u.classList.remove("active"));
          // Activar el usuario actual
          el.classList.add("active");

          const username = el.dataset.username;
          this.onUserSelected(username);
        });
      });
    });

    return div;
  }

}
