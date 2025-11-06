export class Chat {
  constructor() {
    this.messages = [];
    this.container = document.createElement("div");
  }

  addMessage(text) {
    const msg = { text, time: new Date().toLocaleTimeString() };
    this.messages.push(msg);
    this.renderMessages();
  }

  renderMessages() {
    this.container.innerHTML = "";
    this.messages.forEach((m) => {
      const div = document.createElement("div");
      div.classList.add("message");
      div.textContent = `${m.text} (${m.time})`;
      this.container.appendChild(div);
    });
  }

  render() {
    this.container.classList.add("messages");
    this.renderMessages();
    return this.container;
  }
}
