import { Menu } from "../components/Menu.js";
import { GroupList } from "../components/GroupList.js";
import { Chat } from "../components/Chat.js";
import { MessageInput } from "../components/MessageInput.js";

export class GroupPage {
  constructor(router) {
    this.router = router;
  }

  render() {
    const box = document.createElement("div");
    box.id = "box";
    const container = document.createElement("div");
    container.classList.add("container");

    const sidebar = new GroupList(this.router).render();
    const chatArea = document.createElement("div");
    chatArea.classList.add("chat-area");
    chatArea.innerHTML = `
      <div class="top-chat sidebar-text">Chat con grupo_n</div>
    `;

    const content = document.createElement("div");
    content.classList.add("chat-area");

    const header = new Menu(this.router).render();
    const chat = new Chat().render();

    chatArea.append(chat);
    container.append(sidebar, chatArea);
    box.append(header, container);

    return box;
  }
}
