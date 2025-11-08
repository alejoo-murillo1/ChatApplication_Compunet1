const express = require('express');
const net = require('net');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const port = 3001;

app.post("/users", (req, res) => {
  const userData = req.body;

  // crear un nuevo socket para cada petición
  const socket = new net.Socket();

  socket.connect(5000, "localhost", () => {
    const message = JSON.stringify({
      action: "register_user",
      data: userData,
    });

    console.log("Enviando al servidor TCP:", message);
    socket.write(message + "\n");
  });

  socket.on("data", (data) => {
    try {
      const response = JSON.parse(data.toString());
      console.log("Respuesta del servidor TCP:", response);
      res.json(response);
      socket.end(); // cerrar después de recibir respuesta
    } catch (err) {
      console.error("Error procesando respuesta:", err);
      res.status(500).json({ status: "error", body: "Respuesta inválida del servidor TCP" });
      socket.destroy();
    }
  });

  socket.on("error", (err) => {
    console.error("Error en la conexión TCP:", err.message);
    res.status(500).json({ status: "error", body: "Error en la conexión TCP" });
  });

  socket.on("close", () => {
    console.log("Conexión TCP cerrada");
  });
});


app.listen(port, () => {
  console.log(`Proxy HTTP escuchando en http://localhost:${port}`);
});