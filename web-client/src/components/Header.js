export class Header {
  constructor(title) {
    this.title = title;
  }

  render() {
    const div = document.createElement("div");
    div.classList.add("header");
    div.textContent = this.title;
    return div;
  }
}
