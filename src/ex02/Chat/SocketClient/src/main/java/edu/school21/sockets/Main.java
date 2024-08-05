package edu.school21.sockets;

import com.google.gson.Gson;
import edu.school21.sockets.models.Data;

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
        Gson gson = new Gson();
        try (Socket socket = new Socket("localhost", Integer.parseInt(port[1]));
             BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleBufferedReader = new BufferedReader(new InputStreamReader(System.in));
             PrintStream printStream = new PrintStream(socket.getOutputStream())) {
            int lastResult = 1;
            while (lastResult != 0 && lastResult != 4) {
                if (lastResult == 1) {
                    if (socketBufferedReader.ready()) {
                        String readMessage = socketBufferedReader.readLine();
                        Data data = gson.fromJson(readMessage, Data.class);
                        lastResult = data.getState();
                        if (data.getMessage() != null) System.out.println(data.getMessage());
                    }
                } else if (lastResult == 2 || lastResult == 3) {
                    String message = consoleBufferedReader.readLine();
                    int value = 0;
                    if (lastResult == 2) {
                        try {
                            value = Integer.parseInt(message);
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        message = null;
                    }
                    lastResult = 1;
                    String writeMessage = gson.toJson(new Data(1, value, message));
                    printStream.print(writeMessage);
                }
            }
            while (lastResult == 4) {
                if (socketBufferedReader.ready()) {
                    String readMessage = socketBufferedReader.readLine();
                    Data data = gson.fromJson(readMessage, Data.class);
                    lastResult = data.getState();
                    if (lastResult == 0) {
                        System.out.println("You have left the chat.");
                    }
                    System.out.println(data.getMessage());
                }

                if (consoleBufferedReader.ready()) {
                    String message = consoleBufferedReader.readLine();
                    if (message.equals("Exit")) {
                        lastResult = 0;
                        System.out.println("You have left the chat.");
                    }
                    message = gson.toJson(new Data(lastResult, 0, message));
                    printStream.print(message);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
