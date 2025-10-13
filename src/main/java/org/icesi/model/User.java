package org.icesi.model;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private String status; // online, offline, in_call

    public User(int id, String username) {
        this.id = id;
        this.username = username;
        this.status = "online";
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return username + " (" + status + ")";
    }
}
