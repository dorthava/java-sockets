package edu.school21.sockets.app;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.server.Server;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Message message = new Message("1", "2", Timestamp.valueOf(LocalDateTime.now()));
        if(args.length != 1) {
            System.err.println("Можно передать только один аргумент вида: \"--port=8081\"");
            return;
        }

        String[] port = args[0].split("=");
        if(port.length != 2) {
            System.err.println("Error argument");
            return;
        }

        try {
            Server server = new Server(Integer.parseInt(port[1]));
            server.start();
        } catch (NumberFormatException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
