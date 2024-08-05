package edu.school21.sockets.app;

import edu.school21.sockets.server.Server;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
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
