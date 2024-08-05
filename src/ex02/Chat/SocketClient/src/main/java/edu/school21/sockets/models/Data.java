package edu.school21.sockets.models;

public class Data {
    private int state;
    private int value;
    private String message;

    public Data(int state, int value, String message) {
        this.state = state;
        this.value = value;
        this.message = message;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
