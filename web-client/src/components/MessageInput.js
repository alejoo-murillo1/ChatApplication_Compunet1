export class MessageInput {
  constructor(chat) {
    this.chat = chat;
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("message-input");
    div.innerHTML = `
      <input id="msg" type="text" placeholder="Escribe un mensaje">
      <button id="send" class="button-on-off" disabled>Enviar</button>
    `;

    const input = div.querySelector("#msg");
    const button = div.querySelector("#send");

    input.addEventListener("input", () => {
      if (input.value.trim() !== "") {
        button.classList.add("active");
        button.disabled = false;
      } else {
        button.classList.remove("active");
        button.disabled = true;
      }
    });

    button.addEventListener("click", async () => {;
      const text = input.value.trim();
      if (text) {
        this.chat.addMessage(text, "me", "you");
        input.value = "";
      }
    });

    return div;
  }
}
