const express = require('express');
const net = require('net');
const cors = require('cors');

const socket = new net.Socket();
let connected = false;

socket.connect(5000, "localhost", () => {
    connected = true;
})

const app = express();
app.use(cors());
const port = 3001;
app.use(express.json());