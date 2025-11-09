import { Menu } from "../components/Menu.js";
import { GroupList } from "../components/GroupList.js";
import { Chat } from "../components/Chat.js";

export class GroupPage {
  constructor(router) {
    this.router = router;
    this.selectedGroup;
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
      <p class="light-text">No ha seleccionado ningún grupo</p>
    </div>`;

    // callback cuando se selecciona un grupo
    const onGroupSelected = (groupName) => {
      console.log("groupName recibido:", groupName);
      this.selectedGroup = groupName;
      chatArea.innerHTML = `
        <div class="top-chat sidebar-text">Chat ${groupName}</div>
      `;
      const chat = new Chat(groupName).render();
      chatArea.append(chat);
    };


    // pasamos el callback al crear el UserList
    const sidebar = new GroupList(this.router, onGroupSelected).render();

    container.append(sidebar, chatArea);
    box.append(header, container);

    return box;
  }
}
