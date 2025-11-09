import axios from "axios";

export class GroupList {
  constructor(router, onGroupSelected) {
    this.router = router;
    this.onGroupSelected = onGroupSelected;
  }

  async fetchGroups(username) {
    try {
      const response = await axios.get("http://localhost:3001/groups", {
        params: {username}
      });
      console.log("Respuesta del proxy:", response.data);

      if (response.data.status === "ok") {
        console.log("Grupos obtenidos:", response.data.body);
      } else if (response.data.status === "warning"){
        console.log("No pertenece a ningún grupo");
      }

      return response.data;
    } catch (error) {
      console.error("Error al obtener grupos:", error);
      return { status: "error", data: { message: "Error al conectar con el servidor" } };
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

    this.fetchGroups(name).then(response => {
      const { status, data } = response;

      let content = "";
      
      if (status === "ok") {
        const groups = data.groups || [];
        
        content = groups.map(u => `<div class="group" data-username="${u}">${u}</div>`).join("");
      } 
      else if (status === "warning") {
        content = `<p class="light-text">No tiene grupos creados</p>`;
      } 
      else {
        content = `<p class="light-text">Ocurrió un error al obtener los grupos</p>`;
      }

      div.innerHTML = `
        <h3 class="sidebar-text">Grupos conectados</h3>
        <div class="group-list">
          ${content}
        </div>
        <p class="light-text">Selecciona un grupo para enviar un mensaje</p>
      `;

      // Escucha de clicks en los usuarios
      div.querySelectorAll(".group").forEach(el => {
        el.addEventListener("click", () => {
          // Quitar el estado activo anterior
          div.querySelectorAll(".group").forEach(u => u.classList.remove("active"));
          // Activar el usuario actual
          el.classList.add("active");

          const groupSelected = el.dataset.username;
          this.onGroupSelected(groupSelected);
        });
      });
    });

    return div;
  }
}
