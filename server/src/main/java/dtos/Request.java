package dtos;

import com.google.gson.JsonObject;

public class Request {
    private String action;
    private JsonObject data;

    public Request(String action, JsonObject data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }
    public JsonObject getData() {
        return data;
    }
    public void setaction(String action) {
        this.action = action;
    }
    public void setData(JsonObject data) {
        this.data = data;
    }
}