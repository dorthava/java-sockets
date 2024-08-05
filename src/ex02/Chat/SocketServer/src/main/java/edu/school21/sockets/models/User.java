package edu.school21.sockets.models;

public class User {
    private Long id;
    private String name;
    private String password;
    private long currentRoom;
    private int registrationLevel;

    public User() {
        this.name = null;
        this.password = null;
        registrationLevel = 0;
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        registrationLevel = 4;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(long currentRoom) {
        this.currentRoom = currentRoom;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRegistrationLevel() {
        return registrationLevel;
    }

    public void setRegistrationLevel(int registrationLevel) {
        this.registrationLevel = registrationLevel;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
