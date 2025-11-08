import { Menu } from "../components/Menu.js";
import { UserList } from "../components/UserList.js";
import { Chat } from "../components/Chat.js";
import { MessageInput } from "../components/MessageInput.js";

export class ChatPage {
  constructor(router) {
    this.router = router;
    this.selectedUser = null;
  }

  render() {
    const box = document.createElement("div");
    box.id = "box";
    const container = document.createElement("div");
    container.classList.add("container");

    const header = new Menu(this.router).render();
    const chatArea = document.createElement("div");
    chatArea.classList.add("chat")

    chatArea.innerHTML = `
    <div class="no-user-selected">
      <p class="light-text">No ha seleccionado ningún usuario</p>
    </div>`;

    // callback cuando se selecciona un usuario
    const onUserSelected = (username) => {
      this.selectedUser = username;
      chatArea.innerHTML = `
        <div class="top-chat sidebar-text">Chat con ${username}</div>
      `;
      const chat = new Chat().render();
      chatArea.append(chat);
    };

    // pasamos el callback al crear el UserList
    const sidebar = new UserList(this.router, onUserSelected).render();

    container.append(sidebar, chatArea);
    box.append(header, container);

    return box;
  }
}
