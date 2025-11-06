export class GroupList {
  constructor(groups = []) {
    this.groups = groups;
  }

  render() {
    const ul = document.createElement("ul");
    ul.style.listStyle = "none";
    ul.style.padding = "10px";

    this.groups.forEach((g) => {
      const li = document.createElement("li");
      li.textContent = g;
      li.style.cursor = "pointer";
      li.style.padding = "5px 0";
      ul.appendChild(li);
    });

    return ul;
  }
}
