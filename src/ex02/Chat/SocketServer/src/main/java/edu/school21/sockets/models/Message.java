package edu.school21.sockets.models;

import java.sql.Timestamp;

public class Message {
    private Long id;
    private Long sender;
    private Long room;
    private String text;
    private Timestamp timestamp;

    public Message(Long sender, Long room, String text, Timestamp timestamp) {
        this.sender = sender;
        this.room = room;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
    }

    public Long getRoom() {
        return room;
    }

    public void setRoom(Long room) {
        this.room = room;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
