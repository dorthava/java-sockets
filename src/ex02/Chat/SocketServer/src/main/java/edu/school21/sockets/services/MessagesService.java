package edu.school21.sockets.services;

import edu.school21.sockets.models.Message;

import java.util.List;


public interface MessagesService {
    boolean saveMessage(Message message);

    List<Message> findAllInRoom(Long id);
}
