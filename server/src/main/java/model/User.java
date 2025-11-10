package model;

public class User {
    private String name;
    private boolean online; //true = is connected. false = is disconnected

    public User(String name, boolean online) {
        this.name = name;
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
