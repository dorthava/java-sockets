package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MessagesRepository extends CrudRepository<Message> {
    Optional<Message> findById(Long id);
    List<Message> findAllInRoom(Long id);
}
