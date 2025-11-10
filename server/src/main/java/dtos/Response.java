package dtos;

import com.google.gson.JsonObject;

public class Response {
    private String status;
    private JsonObject data;

    public Response() {
    }

    public String getstatus() {
        return status;
    }
    public JsonObject getData() {
        return data;
    }
    public void setstatus(String status) {
        this.status = status;
    }
    public void setData(JsonObject data) {
        this.data = data;
    }
}
