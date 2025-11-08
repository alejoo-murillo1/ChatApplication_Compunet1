import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Request;
import dtos.Response;
import model.User;
import services.ServerServices;

public class Server {

    private Gson gson;
    private ServerServices services;
    private boolean running;

    public static void main(String[] args) throws Exception {
        new Server();
    }

    public Server() throws Exception {
        gson = new Gson();
        services = new ServerServices();
        int port = 5000;
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Server running on port: " + port);
        running = true;
        while (running) {
            Socket sc = socket.accept();
            handleClient(sc);
        }
        socket.close();
    }

    public void handleClient(Socket socket){
        try{
            System.out.println("New client connected from port " + socket.getPort());
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String msg = in.readLine();

            System.out.println("Message received:" + msg);

            Request rq = gson.fromJson(msg, Request.class);

            try {
                Response res = handleRequest(rq);

                System.out.println("Response: " + gson.toJson(res));

                out.println(gson.toJson(res));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            System.out.println("IO Error");
            throw new RuntimeException(e);
        } finally {
            try {
                System.out.println("Connection closed");
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Response handleRequest(Request rq)throws Exception {
        Response resp = new Response();
        switch (rq.getAction()) {
            case "register_user":
                try {
                    String name = rq.getData().get("name").getAsString();
                    boolean online = rq.getData().get("online").getAsBoolean();
                    User user = new User(name, online);

                    User newUser = services.registerUser(user);
                    System.out.println(newUser);

                    if(newUser != null){
                        resp.setstatus("ok");
                        resp.setData(gson.toJsonTree(newUser).getAsJsonObject());
                    }
                } catch (Exception e) {
                    resp.setstatus("error");
                    resp.setData(gson.toJsonTree(Map.of("message", "User registration failed")).getAsJsonObject());
                    return resp;
                }
                break;

            case "get_online_users":
                try {
                    List<String> namesOnlineUsers = services.getOnlineUsers();
                    System.out.println(namesOnlineUsers.toString());

                    if(namesOnlineUsers.size() > 1){
                        resp.setstatus("ok");
                        resp.setData(
                            gson.toJsonTree(Map.of("users", namesOnlineUsers)).getAsJsonObject()
                        );
                    } else {
                        resp.setstatus("warning");
                        resp.setData(gson.toJsonTree(Map.of("message", "Only one user registered")).getAsJsonObject());
                    }
                } catch (Exception e) {
                    resp.setstatus("error");
                    resp.setData(gson.toJsonTree(Map.of("message", "Get users online failed")).getAsJsonObject());
                    return resp;
                }
                break;
                
            default:
                resp.setstatus("error");
                resp.setData(gson.toJsonTree(Map.of("message", "Unknown action")).getAsJsonObject());
                break;
        }
        
        return resp;
    }

}
