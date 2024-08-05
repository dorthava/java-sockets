package edu.school21.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Можно передать только один аргумент вида: \"--port=8081\"");
            return;
        }

        String[] port = args[0].split("=");
        if (port.length != 2) {
            System.err.println("Error argument");
            return;
        }

        try (Socket socket = new Socket("localhost", Integer.parseInt(port[1]));
             BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleBufferedReader = new BufferedReader(new InputStreamReader(System.in));
             PrintStream printStream = new PrintStream(socket.getOutputStream())) {
            while(true) {
                if(socketBufferedReader.ready()) {
                    String readMessage = socketBufferedReader.readLine();
                    if(readMessage.equals("Unsuccessful!") || readMessage.equals("Exit")) {
                        if(socketBufferedReader.readLine() == null) break;
                    }
                    System.out.println(readMessage);
                }

                if(consoleBufferedReader.ready()) {
                    String message = consoleBufferedReader.readLine();
                    if(message.equals("Exit")) {
                        System.out.println("You have left the chat.");
                        break;
                    }
                    printStream.print(message);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
