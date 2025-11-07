import axios from "axios";

export class HomePage {
  constructor(router) {
    this.router = router;
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("home");
    div.innerHTML = `
      <h1 id="welcome">Bienvenid@</h1>
      <p id="descr">Este es un chat en línea para enviar mensajes a las personas conectadas</p>
      <div class="container" id="form">
        <input id="username" type="text" placeholder="Ingresa el nombre con el que quieres aparecer" />
        <button id="enter" class="button-on-off" disabled>Entrar</button>
        <div class="image-box">
        </div>
      </div>
    `;

    const input = div.querySelector("#username");
    const button = div.querySelector("#enter");

    input.addEventListener("input", () => {
      if (input.value.trim() !== "") {
        button.classList.add("active");
        button.disabled = false;
      } else {
        button.classList.remove("active");
        button.disabled = true;
      }
    });

    button.addEventListener("click", async () => {
      const name = input.value.trim();
      if (name) {
        sessionStorage.setItem("username", name);


        await this.sendUserToServer(name);

        // Luego navegar al chat
        this.router.navigateTo("/chat");
      }
    });


    return div;
  }

  async sendUserToServer(name) {
    try {
      
      const userData = {
        name: name,
        status: "online",
      };

      const response = await axios.post("http://localhost:3001/users", userData);

      console.log("Usuario registrado correctamente:", response.data);
    } catch (error) {
      console.error("Error al registrar el usuario:", error);
    }
  }


}
