package edu.school21.sockets.server;

import edu.school21.sockets.config.SocketsApplicationConfig;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;
import edu.school21.sockets.services.MessageService;
import edu.school21.sockets.services.UsersService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ByteBuffer buffer;
    private final Map<SocketChannel, User> users = new HashMap<>();
    UsersService usersService;
    MessageService messageService;

    public Server(int port) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
        usersService = applicationContext.getBean("usersService", UsersService.class);
        messageService = applicationContext.getBean("messagesService", MessageService.class);
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
        boolean running = true;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (running) {
            selector.select();
            if(bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if(line.equals("Exit")) {
                    running = false;
                }
            }
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
        }
        serverSocketChannel.close();
        for(Map.Entry<SocketChannel, User> userEntry : users.entrySet()) {
            SocketChannel socketChannel = userEntry.getKey();
            buffer.clear();
            buffer.put("Exit\n".getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            socketChannel.close();
        }
    }

    private void accept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        buffer.clear();
        buffer.put("Hello from Server!\n".getBytes());
        buffer.flip();
        socketChannel.write(buffer);
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
        if (message.equalsIgnoreCase("Exit")) {
            users.remove(socketChannel);
            socketChannel.write(buffer);
            socketChannel.close();
            return;
        } else if (message.equalsIgnoreCase("signUp")) {
            user.setRegistrationLevel(1);
        } else if (message.equalsIgnoreCase("signIn")) {
            user.setRegistrationLevel(5);
        }

        if(user.getRegistrationLevel() == 9) {
            buffer.clear();
            buffer.put((user.getName() + ": " + message + "\n").getBytes());
            buffer.flip();
            Message newMessage = new Message(user.getName(), message, Timestamp.valueOf(LocalDateTime.now()));
            messageService.saveMessage(newMessage);
            for(Map.Entry<SocketChannel, User> userEntry : users.entrySet()) {
                SocketChannel key = userEntry.getKey();
                User findUser = userEntry.getValue();
                if(key == socketChannel || findUser.getRegistrationLevel() != 9) continue;
                key.write(buffer);
            }
        } else if ((user.getRegistrationLevel() > 0 && user.getRegistrationLevel() < 5) ||
                (user.getRegistrationLevel() > 4 && user.getRegistrationLevel() < 9)) {
            boolean result = registration(user, socketChannel, message);
            if (!result) {
                users.remove(socketChannel);
                socketChannel.close();
            }
        }
    }

    private boolean registration(User user, SocketChannel socketChannel, String message) throws IOException {
        boolean result = true;
        if (user.getRegistrationLevel() == 1 || user.getRegistrationLevel() == 5) {
            buffer.clear();
            buffer.put("Enter username:\n".getBytes());
            buffer.flip();
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
            socketChannel.write(buffer);
        } else if (user.getRegistrationLevel() == 2 || user.getRegistrationLevel() == 6) {
            user.setName(message);
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
        }

        if (user.getRegistrationLevel() == 3 || user.getRegistrationLevel() == 7) {
            buffer.clear();
            buffer.put("Enter password:\n".getBytes());
            buffer.flip();
            user.setRegistrationLevel(user.getRegistrationLevel() + 1);
            socketChannel.write(buffer);
        } else if (user.getRegistrationLevel() == 4 || user.getRegistrationLevel() == 8) {
            user.setPassword(message);
            if(user.getRegistrationLevel() == 4) {
                result = usersService.signUp(user.getName(), user.getPassword());
            } else {
                result = usersService.signIn(user.getName(), user.getPassword());
            }
            buffer.clear();
            if (result) {
                buffer.put("Successful!\nStart messaging\n".getBytes());
            } else {
                buffer.put("Unsuccessful!\n".getBytes());
            }
            buffer.flip();
            socketChannel.write(buffer);
            user.setRegistrationLevel(9);
        }
        return result;
    }
}
