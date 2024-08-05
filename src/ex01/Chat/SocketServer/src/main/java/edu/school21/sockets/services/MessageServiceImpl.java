package edu.school21.sockets.services;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.repositories.MessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("messagesService")
public class MessageServiceImpl implements MessageService {
    private final MessagesRepository messagesRepository;

    @Autowired
    public MessageServiceImpl(MessagesRepository messagesRepository) {
        this.messagesRepository = messagesRepository;
    }

    @Override
    public boolean saveMessage(Message message) {
        return messagesRepository.save(message) == 1;
    }
}
