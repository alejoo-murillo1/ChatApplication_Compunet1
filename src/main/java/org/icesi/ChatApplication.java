package org.icesi;

import org.icesi.client.ChatConsole;
import org.icesi.server.ChatServer;

import java.io.IOException;

public class ChatApplication {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("server")) {
            try {
                System.out.println("╔════════════════════════════════╗");
                System.out.println("║  INICIANDO SERVIDOR DE CHAT    ║");
                System.out.println("╚════════════════════════════════╝");
                ChatServer server = new ChatServer();
                server.start();
            } catch (Exception e) {
                System.err.println("[ERROR] No se pudo iniciar el servidor: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("╔════════════════════════════════╗");
            System.out.println("║  INICIANDO CLIENTE DE CHAT     ║");
            System.out.println("╚════════════════════════════════╝");
            ChatConsole console = new ChatConsole();
            console.start();
        }
    }
}