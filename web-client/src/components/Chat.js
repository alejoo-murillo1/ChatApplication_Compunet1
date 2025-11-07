import { MessageInput } from "./MessageInput.js";

export class Chat {
  constructor() {
    this.messages = [];
  }

  render() {
    // contenedor principal del chat (mensajes + input)
    this.wrapper = document.createElement("div");
    this.wrapper.classList.add("chat-wrapper");
    this.wrapper.style.display = "flex";
    this.wrapper.style.flexDirection = "column";
    this.wrapper.style.flex = "1";

    // contenedor de mensajes
    this.div = document.createElement("div");
    this.div.classList.add("chat");
    this.div.style.flex = "1";
    this.div.style.padding = "10px";
    this.div.style.overflowY = "auto";

    // crear el input de mensajes (le pasamos this)
    const messageInput = new MessageInput(this).render();

    // agregar mensajes + input al wrapper
    this.wrapper.append(this.div, messageInput);

    return this.wrapper;
  }

  addMessage(text, sender, receiver) {
    const msg = document.createElement("div");
    msg.classList.add("message");
    if (sender !== "me") msg.classList.add("received");
    msg.textContent = text;
    this.div.appendChild(msg);

    // desplazarse al final automáticamente
    this.div.scrollTop = this.div.scrollHeight;
  }
}
