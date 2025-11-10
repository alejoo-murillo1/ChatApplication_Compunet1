import axios from "axios";
import { MessageInput } from "./MessageInput.js";

export class Chat {
  constructor(receiver, isGroup = false) {
    this.receiver = receiver; // puede ser un username o nombre del grupo
    this.isGroup = isGroup;
    this.messages = [];
  }

  render() {
    this.wrapper = document.createElement("div");
    this.wrapper.classList.add("chat-wrapper");

    this.div = document.createElement("div");
    this.div.classList.add("msg-container");

    const messageInput = new MessageInput(this).render();

    this.wrapper.append(this.div, messageInput);

    // cargar mensajes según tipo
    this.loadMessages();

    return this.wrapper;
  }

  async loadMessages() {
    try {
      const sender = sessionStorage.getItem("username");

      const response = await axios.get("http://localhost:3001/get_messages", {
          params: { sender, receiver: this.receiver },
        });
      console.log("Mensaje recibido del proxy:", response.data);

      if (response.data.status === "ok") {
        this.renderMessages(response.data.data.messages);
      } else if (response.data.status === "warning"){
        this.div.innerHTML = `<p class="light-text">No hay mensajes</p>`;
      }

    } catch (error) {
      console.error("Error al obtener mensajes:", error);
      this.div.innerHTML = `<p class="light-text">Error al obtener mensajes</p>`;
    }
  }

  renderMessages(messages) {
    this.div.innerHTML = "";
    const currentUser = sessionStorage.getItem("username");

    messages.forEach(({ sender, message }) => {
      // contenedor del mensaje completo (nombre + burbuja)
      const msgWrapper = document.createElement("div");
      msgWrapper.classList.add("chat-message-wrapper");

      // nombre del remitente (solo si es grupo o no soy yo)
      if (this.isGroup && sender !== currentUser) {
        const senderLabel = document.createElement("div");
        senderLabel.classList.add("chat-sender");
        senderLabel.textContent = sender;
        msgWrapper.appendChild(senderLabel);
      }

      // burbuja del mensaje
      const msgBubble = document.createElement("div");
      msgBubble.classList.add("chat-message");
      msgBubble.textContent = message;

      if (sender === currentUser) {
        msgBubble.classList.add("sent");
      } else {
        msgBubble.classList.add("received");
      }

      msgWrapper.appendChild(msgBubble);
      this.div.appendChild(msgWrapper);
    });

    this.div.scrollTop = this.div.scrollHeight;
  }

}
