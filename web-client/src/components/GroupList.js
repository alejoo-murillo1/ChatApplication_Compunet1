import axios from "axios";

export class GroupList {
  constructor(router) {
    this.router = router;
  }

  async fetchGroups(username) {
    try {
      const response = await axios.get("http://localhost:3001/groups", {
      params: { username }
      });
      return response.data;

    } catch (error) {
      console.error("Error al obtener grupos:", error);
      return [];
    }
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("sidebar");

    div.innerHTML = `
      <h3 class="sidebar-text">Grupos</h3>
      <p class="light-text">Cargando grupos...</p>
    `;

    const name = sessionStorage.getItem("username") || "Invitado";

    console.log("Obteniendo grupos para el usuario:", name);

    this.fetchGroups(name).then(groups => {
      div.innerHTML = `
        <h3 class="sidebar-text">Grupos</h3>
        ${groups.map(u => `<div class="group">${u}</div>`).join("")}
        <p class="light-text">Selecciona un grupo para enviarle un mensaje</p>
      `;
    });

    return div;
  }
}
