package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;

import java.sql.Timestamp;
import java.util.Optional;

public interface MessagesRepository extends CrudRepository<Message> {
    Optional<Message> findByTimestamp(Timestamp timestamp);
}
