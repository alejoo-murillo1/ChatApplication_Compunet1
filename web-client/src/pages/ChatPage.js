import { Header } from "../components/Header.js";
import { Chat } from "../components/Chat.js";
import { MessageInput } from "../components/MessageInput.js";
import { GroupList } from "../components/GroupList.js";

export class ChatPage {
  constructor(router) {
    this.router = router;
  }

  render() {
    const container = document.createElement("div");
    container.classList.add("chat-container");

    const header = new Header("Chat General");
    const chat = new Chat();
    const input = new MessageInput((msg) => chat.addMessage(msg));
    const groups = new GroupList(["Amigos", "Trabajo", "Familia"]);

    container.appendChild(header.render());
    container.appendChild(chat.render());
    container.appendChild(input.render());
    // Puedes añadir el grupo a un sidebar en el futuro

    return container;
  }
}
