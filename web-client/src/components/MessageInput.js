export class MessageInput {
  constructor(onSend) {
    this.onSend = onSend;
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("message-input");

    const input = document.createElement("input");
    input.placeholder = "Escribe un mensaje...";

    const button = document.createElement("button");
    button.textContent = "Enviar";

    button.onclick = () => {
      const value = input.value.trim();
      if (value) {
        this.onSend(value);
        input.value = "";
      }
    };

    div.appendChild(input);
    div.appendChild(button);
    return div;
  }
}
