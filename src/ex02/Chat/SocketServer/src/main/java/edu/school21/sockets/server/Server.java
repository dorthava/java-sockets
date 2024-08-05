package edu.school21.sockets.server;

import com.google.gson.Gson;
import edu.school21.sockets.config.SocketsApplicationConfig;
import edu.school21.sockets.models.Data;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.Room;
import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.RoomsRepository;
import edu.school21.sockets.services.MessagesService;
import edu.school21.sockets.services.UsersService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Server {
    private final Gson gson;
    private final Map<SocketChannel, User> users = new HashMap<>();
    private final UsersService usersService;
    private final MessagesService messagesService;
    private final RoomsRepository roomsRepository;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ByteBuffer buffer;

    public Server(int port) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
        usersService = applicationContext.getBean("usersService", UsersService.class);
        messagesService = applicationContext.getBean("messagesService", MessagesService.class);
        roomsRepository = applicationContext.getBean("roomsRepository", RoomsRepository.class);
        gson = new Gson();
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            buffer = ByteBuffer.allocate(4048);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void start() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    accept();
                } else if (key.isReadable()) {
                    read(key);
                }
            }
            if (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.equals("Exit")) {
                    break;
                }
            }
        }
        serverSocketChannel.close();
        for (Map.Entry<SocketChannel, User> userEntry : users.entrySet()) {
            sendMessage(userEntry.getKey(), 0, "Server close.");
            userEntry.getKey().close();
        }
    }

    private void accept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        sendMessage(socketChannel, 2, "Hello from Server!\n1. signIn\n2. signUp\n3. Exit");
        users.put(socketChannel, new User());
    }

    private void read(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        User user = users.get(socketChannel);
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            System.out.println("Connection closed");
            users.remove(socketChannel);
            socketChannel.close();
            return;
        }
        String message = new String(buffer.array(), 0, bytesRead);
        Data data = gson.fromJson(message, Data.class);
        message = data.getMessage();
        if (data.getState() == 0 || (data.getValue() == 3 && (user.getRegistrationLevel() == 0 || user.getRegistrationLevel() == 10))) {
            exitInServer(socketChannel, "Connection closed");
            System.out.println("Connection closed");
            return;
        }
        if (user.getRegistrationLevel() == 0) {
            if (data.getValue() == 2) {
                user.setRegistrationLevel(1);
            } else if (data.getValue() == 1) {
                user.setRegistrationLevel(5);
            } else {
                sendMessage(socketChannel, 2, null);
            }
        }
        if (user.getRegistrationLevel() == 14) {
            String sendMessage = user.getName() + ": " + message;
            Message newMessage = new Message(user.getId(), user.getCurrentRoom(), message, Timestamp.valueOf(LocalDateTime.now()));
            messagesService.saveMessage(newMessage);
            for (Map.Entry<SocketChannel, User> userEntry : users.entrySet()) {
                SocketChannel key = userEntry.getKey();
                User findUser = userEntry.getValue();
                if (key == socketChannel || findUser.getRegistrationLevel() != 14 && findUser.getCurrentRoom() != user.getCurrentRoom())
                    continue;
                sendMessage(key, 4, sendMessage);
            }
        } else if (user.getRegistrationLevel() == 12) {
            long choice = data.getValue();
            long count = roomsRepository.findCount();
            if (choice == count + 1) {
                System.out.println("Connection closed");
                exitInServer(socketChannel, null);
            } else if (choice <= count) {
                long id = 0;
                for (Room room : roomsRepository.findAll()) {
                    ++id;
                    if (id != choice) continue;
                    sendMessage(socketChannel, 1, room.getName() + " ---");
                    break;
                }
                user.setRegistrationLevel(13);
                user.setCurrentRoom(id);
            }
        } else if (user.getRegistrationLevel() == 11) {
            roomsRepository.save(new Room(null, message, user.getId()));
            Optional<Room> optionalRoom = roomsRepository.findByName(message);
            if (optionalRoom.isPresent()) {
                user.setCurrentRoom(optionalRoom.get().getId());
                sendMessage(socketChannel, 1, "Successful!\n" + optionalRoom.get().getName() + " ---");
            }
        } else if (user.getRegistrationLevel() == 10) {
            setRoom(socketChannel, user, data.getValue());
        } else if ((user.getRegistrationLevel() > 0 && user.getRegistrationLevel() < 5) ||
                (user.getRegistrationLevel() > 4 && user.getRegistrationLevel() < 9)) {
            boolean result = registration(user, socketChannel, message);
            if (!result) {
                String errorMessage = user.getRegistrationLevel() > 4 ? "Ошибка в логине/пароле" : "Данный пользователь уже существует";
                exitInServer(socketChannel, errorMessage);
            }
        }
        if (user.getRegistrationLevel() == 13) {
            printLastThirtyMessages(user, socketChannel, user.getCurrentRoom());
        }
    }

    private boolean registration(User user, SocketChannel socketChannel, String message) throws IOException {
        boolean result = true;
        if (user.getRegistrationLevel() == 1 || user.getRegistrationLevel() == 5) {
            sendMessage(socketChannel, 3, "Enter username:");
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
        } else if (user.getRegistrationLevel() == 2 || user.getRegistrationLevel() == 6) {
            if (user.getRegistrationLevel() == 2 && usersService.findByName(message).isPresent()) {
                result = false;
            }
            user.setName(message);
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
        }
        if (user.getRegistrationLevel() == 3 || user.getRegistrationLevel() == 7) {
            sendMessage(socketChannel, 3, "Enter password:");
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
        } else if (user.getRegistrationLevel() == 4 || user.getRegistrationLevel() == 8) {
            user.setPassword(message);
            if (user.getRegistrationLevel() == 4) {
                result = usersService.signUp(user.getName(), user.getPassword());
            } else {
                result = usersService.signIn(user.getName(), user.getPassword());
            }
            if (result) {
                sendMessage(socketChannel, 2, "Successful!\n1.\tCreate room\n2.\tChoose room\n3.\tExit");
                user.setRegistrationLevel(10);
                Optional<User> optionalUser = usersService.findByName(user.getName());
                optionalUser.ifPresent(value -> user.setId(value.getId()));
            }
        }
        return result;
    }

    private void sendMessage(SocketChannel socketChannel, int state, String message) throws IOException {
        buffer.clear();
        buffer.put((gson.toJson(new Data(state, 0, message)) + '\n').getBytes());
        buffer.flip();
        socketChannel.write(buffer);
    }

    private void setRoom(SocketChannel socketChannel, User user, int value) throws IOException {
        if (value == 1) {
            sendMessage(socketChannel, 3, "Enter room name:");
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
        } else if (value == 2) {
            List<Room> listRoom = roomsRepository.findAll();
            sendMessage(socketChannel, 1, "Rooms");
            long lastId = 1;
            for (Room room : listRoom) {
                sendMessage(socketChannel, 1, lastId++ + ". " + room.getName());
            }
            sendMessage(socketChannel, 2, lastId + ". Exit");
            user.setRegistrationLevel(user.getRegistrationLevel() + 2);
        } else {
            sendMessage(socketChannel, 2, null);
        }
    }

    private void exitInServer(SocketChannel socketChannel, String message) throws IOException {
        users.remove(socketChannel);
        sendMessage(socketChannel, 0, message);
        socketChannel.close();
    }

    private void printLastThirtyMessages(User user, SocketChannel socketChannel, Long roomId) throws IOException {
        for (Message message : messagesService.findAllInRoom(roomId)) {
            Optional<User> optionalUser = usersService.findById(message.getSender());
            if (optionalUser.isPresent()) {
                sendMessage(socketChannel, 1, optionalUser.get().getName() + ": " + message.getText());
            }
        }
        sendMessage(socketChannel, 4, "--- Вы зашли в чат! ---");
        user.setRegistrationLevel(14);
    }
}
