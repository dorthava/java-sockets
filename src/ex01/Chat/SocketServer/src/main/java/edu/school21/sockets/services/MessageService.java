package edu.school21.sockets.services;

import edu.school21.sockets.models.Message;


public interface MessageService {
    boolean saveMessage(Message message);
}
