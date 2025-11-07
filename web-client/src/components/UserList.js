import axios from "axios";

export class UserList {
  constructor(router) {
    this.router = router;
  }

  async fetchUsers() {
    try {
      const response = await axios.get("http://localhost:3001/users");
      return response.data;

    } catch (error) {
      console.error("Error al obtener usuarios:", error);
      return [];
    }
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("sidebar");

    div.innerHTML = `
      <h3 class="sidebar-text">Usuarios conectados</h3>
      <p class="light-text">Cargando usuarios...</p>
    `;

    this.fetchUsers().then(users => {
      div.innerHTML = `
        <h3 class="sidebar-text">Usuarios conectados</h3>
        ${users.map(u => `<div class="user">${u}</div>`).join("")}
        <p class="light-text">Selecciona un usuario para enviarle un mensaje</p>
      `;
    });

    return div;
  }

}
