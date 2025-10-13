package org.icesi.client;

import org.icesi.audio.AudioRecorder;

import java.util.Scanner;

public class ChatConsole {
    private ChatClient client;
    private final Scanner scanner;
    private boolean running = true;

    public ChatConsole() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║     APLICACIÓN DE CHAT v1.0    ║");
        System.out.println("╚════════════════════════════════╝\n");

        try {
            System.out.print("Ingrese dirección del servidor (localhost): ");
            String server = scanner.nextLine().trim();
            if (server.isEmpty()) server = "localhost";

            System.out.println("[CONECTANDO] Conectando a " + server + "...");
            client = new ChatClient(server, 5000);

            System.out.print("Ingrese su nombre de usuario: ");
            String username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("[ERROR] El nombre de usuario no puede estar vacío");
                return;
            }

            client.login(username);

            // Esperar a que el login sea procesado
            Thread.sleep(1000);

            showMainMenu();

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.disconnect();
            }
            scanner.close();
        }
    }

    private void showMainMenu() {
        while (running) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║         MENÚ PRINCIPAL         ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║ 1. Enviar mensaje de texto     ║");
            System.out.println("║ 2. Enviar mensaje de voz       ║");
            System.out.println("║ 3. Hacer una llamada           ║");
            System.out.println("║ 4. Ver historial               ║");
            System.out.println("║ 5. Escuchar último audio       ║");
            System.out.println("║ 6. Ver mensajes de voz         ║");
            System.out.println("║ 7. Responder llamada entrante  ║");
            System.out.println("║ 8. Gestionar Grupos            ║");
            System.out.println("║ 9. Salir                       ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("Seleccione una opción (1-9): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    sendTextMessage();
                    break;
                case "2":
                    sendVoiceMessage();
                    break;
                case "3":
                    makeCall();
                    break;
                case "4":
                    viewHistory();
                    break;
                case "5":
                    playLastVoiceMessage();
                    break;
                case "6":
                    listVoiceMessages();
                    break;
                case "7":
                    respondToCall();
                    break;
                case "8":
                    showGroupMenu();
                    break;
                case "9":
                    logout();
                    break;
                default:
                    System.out.println("[ERROR] Opción no válida. Intente de nuevo.");
            }
        }
    }

    private void sendTextMessage() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║      ENVIAR MENSAJE TEXTO      ║");
        System.out.println("╚════════════════════════════════╝");

        client.listUsers();

        System.out.print("\nIngrese nombre de usuario destinatario: ");
        String recipient = scanner.nextLine().trim();

        if (recipient.isEmpty()) {
            System.out.println("[ERROR] El destinatario no puede estar vacío");
            return;
        }

        System.out.print("Ingrese su mensaje: ");
        String message = scanner.nextLine().trim();

        if (message.isEmpty()) {
            System.out.println("[ERROR] El mensaje no puede estar vacío");
        } else {
            client.sendMessage(recipient, message);
        }
    }

    private void sendVoiceMessage() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║      ENVIAR MENSAJE VOZ        ║");
        System.out.println("╚════════════════════════════════╝");

        client.listUsers();

        System.out.print("\nIngrese nombre de usuario destinatario: ");
        String recipient = scanner.nextLine().trim();

        if (recipient.isEmpty()) {
            System.out.println("[ERROR] El destinatario no puede estar vacío");
            return;
        }

        System.out.print("Duración de la grabación en segundos (máximo 30): ");

        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("[ERROR] Debe ingresar un número");
                return;
            }

            int seconds = Integer.parseInt(input);
            if (seconds > 30) {
                seconds = 30;
                System.out.println("[INFO] Duración ajustada a 30 segundos (máximo permitido)");
            }
            if (seconds < 1) {
                seconds = 1;
                System.out.println("[INFO] Duración ajustada a 1 segundo (mínimo permitido)");
            }

            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║      GRABACIÓN EN PROGRESO     ║");
            System.out.println("║  Hable ahora por el micrófono  ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("[AUDIO] Grabando durante " + seconds + " segundos...");

            AudioRecorder recorder = new AudioRecorder();
            byte[] audioData = recorder.recordFull(seconds * 1000);
            recorder.stop();

            if (audioData != null && audioData.length > 0) {
                System.out.println("[AUDIO] Grabación completada (" + audioData.length + " bytes)");
                client.sendVoiceMessage(recipient, audioData);
            } else {
                System.out.println("[ERROR] No se grabó audio. Intente de nuevo.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Ingrese un número válido");
        }
    }

    private void makeCall() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║       INICIAR LLAMADA          ║");
        System.out.println("╚════════════════════════════════╝");

        client.listUsers();

        System.out.print("\nIngrese nombre de usuario a llamar: ");
        String recipient = scanner.nextLine().trim();

        if (recipient.isEmpty()) {
            System.out.println("[ERROR] El destinatario no puede estar vacío");
            return;
        }

        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║  LLAMADA EN PROGRESO           ║");
        System.out.println("║  Escriba 'fin' para terminar   ║");
        System.out.println("╚════════════════════════════════╝");

        System.out.println("[LLAMADA] Llamando a " + recipient + "...");
        client.makeCall(recipient);

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("fin")) {
                client.endCall();
                System.out.println("[LLAMADA] Llamada finalizada");
                break;
            }
        }
    }

    private void viewHistory() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║      HISTORIAL DE MENSAJES     ║");
        System.out.println("╚════════════════════════════════╝");
        client.viewHistory();
    }

    private void logout() {
        System.out.println("\n[CLIENT] Cerrando sesión...");
        client.disconnect();
        running = false;
        System.out.println("[CLIENT] ¡Hasta luego! Gracias por usar Chat App.");
    }

    private void playLastVoiceMessage() {
        client.playLastVoiceMessage();
    }

    private void listVoiceMessages() {
        client.listVoiceMessages();
    }

    private void respondToCall() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║    RESPONDER LLAMADA ENTRANTE   ║");
        System.out.println("╚════════════════════════════════╝");

        System.out.print("Ingrese el ID del usuario que está llamando: ");
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("[ERROR] Debe ingresar un ID válido");
                return;
            }

            int callerId = Integer.parseInt(input);
            System.out.print("¿Desea aceptar la llamada? (si/no): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("si") || response.equals("s")) {
                client.acceptCall(callerId);

                System.out.println("\n╔════════════════════════════════╗");
                System.out.println("║  LLAMADA EN PROGRESO           ║");
                System.out.println("║  Escriba 'fin' para terminar   ║");
                System.out.println("╚════════════════════════════════╝");

                while (true) {
                    String input2 = scanner.nextLine().trim();
                    if (input2.equalsIgnoreCase("fin")) {
                        client.endCall();
                        System.out.println("[LLAMADA] Llamada finalizada");
                        break;
                    }
                }
            } else {
                client.rejectCall(callerId);
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Ingrese un número válido");
        }
    }

    private void showGroupMenu() {
        boolean inGroupMenu = true;
        while (inGroupMenu) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       MENÚ DE GRUPOS           ║");
            System.out.println("╠════════════════════════════════╣");
            System.out.println("║ 1. Crear grupo                 ║");
            System.out.println("║ 2. Agregar miembro a grupo     ║");
            System.out.println("║ 3. Enviar mensaje al grupo     ║");
            System.out.println("║ 4. Enviar audio al grupo       ║");
            System.out.println("║ 5. Reproducir audios de grupo  ║");
            System.out.println("║ 6. Hacer una llamada a grupo   ║");
            System.out.println("║ 7. Volver al menú principal    ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.print("Seleccione una opción (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createGroup();
                    break;
                case "2":
                    addGroupMember();
                    break;
                case "3":
                    sendGroupMessage();
                    break;
                case "4":
                    sendGroupAudio();
                    break;
                case "5":
                    playLastGroupVoiceMessage();
                    break;
                case "6":
                    makeGroupCall();
                    break;
                case "7":
                    inGroupMenu = false;
                    break;
                default:
                    System.out.println("[ERROR] Opción no válida");
            }
        }
    }

    private void playLastGroupVoiceMessage() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║     REPROUCIR AUDIOS GRUPALES    ║");
        System.out.println("╚════════════════════════════════╝");

        client.listGroups();
        System.out.print("\n[INPUT] ID del grupo: ");
        try{
            int groupId = Integer.parseInt(scanner.nextLine().trim());

            client.listGroupVoiceMessages(groupId);

            client.playLastGroupVoiceMessage(groupId);

        }catch(NumberFormatException e){
            System.out.println("[INPUT] ✗ Ingrese un ID válido");
        }
    }

    private void makeGroupCall() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║     LLAMADA GRUPAL             ║");
        System.out.println("╚════════════════════════════════╝");

        client.listGroups();
        System.out.print("\n[INPUT] ID del grupo: ");
        try {
            int groupId = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("\n[GROUP_CALL] ☎ Iniciando llamada grupal...");
            System.out.println("[GROUP_CALL] Escriba 'fin' para terminar");

            client.makeGroupCall(groupId);

            while (true) {
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("fin")) {
                    client.endCall();
                    break;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("[INPUT] ✗ Ingrese un ID válido");
        }
    }

    private void createGroup() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║      CREAR NUEVO GRUPO         ║");
        System.out.println("╚════════════════════════════════╝");

        System.out.print("Ingrese el nombre del grupo: ");
        String groupName = scanner.nextLine().trim();

        if (groupName.isEmpty()) {
            System.out.println("[ERROR] El nombre del grupo no puede estar vacío");
            return;
        }

        client.createGroup(groupName);
    }

    private void addGroupMember() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║    AGREGAR MIEMBRO AL GRUPO    ║");
        System.out.println("╚════════════════════════════════╝");

        client.listGroups();

        System.out.print("Ingrese el ID del grupo: ");
        try {
            int groupId = Integer.parseInt(scanner.nextLine().trim());

            client.listUsers();

            System.out.print("\nIngrese el nombre de usuario a agregar: ");
            String username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("[ERROR] El nombre de usuario no puede estar vacío");
                return;
            }

            client.addMemberToGroup(groupId, username);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Ingrese un ID válido");
        }
    }

    private void sendGroupMessage() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║   ENVIAR MENSAJE AL GRUPO      ║");
        System.out.println("╚════════════════════════════════╝");

        client.listGroups();

        System.out.print("Ingrese el ID del grupo: ");
        try {
            int groupId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Ingrese su mensaje: ");
            String message = scanner.nextLine().trim();

            if (message.isEmpty()) {
                System.out.println("[ERROR] El mensaje no puede estar vacío");
                return;
            }

            client.sendGroupMessage(groupId, message);

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Ingrese un ID válido");
        }
    }

    private void sendGroupAudio() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║   ENVIAR AUDIO AL GRUPO        ║");
        System.out.println("╚════════════════════════════════╝");

        client.listGroups();

        System.out.print("Ingrese el ID del grupo: ");
        try {
            int groupId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Duración de la grabación en segundos (máximo 30): ");

            int seconds = Integer.parseInt(scanner.nextLine().trim());
            if (seconds > 30) {
                seconds = 30;
                System.out.println("[INFO] Duración ajustada a 30 segundos (máximo permitido)");
            }
            if (seconds < 1) {
                seconds = 1;
                System.out.println("[INFO] Duración ajustada a 1 segundo (mínimo permitido)");
            }

            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║      GRABACIÓN EN PROGRESO     ║");
            System.out.println("║  Hable ahora por el micrófono  ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("[AUDIO] Grabando durante " + seconds + " segundos...");

            AudioRecorder recorder = new AudioRecorder();
            byte[] audioData = recorder.recordFull(seconds * 1000);
            recorder.stop();

            if (audioData != null && audioData.length > 0) {
                System.out.println("[AUDIO] Grabación completada (" + audioData.length + " bytes)");
                client.sendGroupVoiceMessage(groupId, audioData);
            } else {
                System.out.println("[ERROR] No se grabó audio. Intente de nuevo.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Ingrese un número válido");
        }
    }
}
