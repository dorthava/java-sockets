package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component("messagesRepository")
public class MessagesRepositoryImpl implements MessagesRepository {
    private final JdbcTemplate jdbcTemplate;
    private final MessageRowMapper messageRowMapper;

    @Autowired
    public MessagesRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.messageRowMapper = new MessageRowMapper();
    }

    @Override
    public Optional<Message> findById(Long id) {
        List<Message> messages = jdbcTemplate.query("SELECT * FROM message WHERE id = ?", messageRowMapper, id);
        return messages.stream().findFirst();
    }

    @Override
    public List<Message> findAllInRoom(Long id) {
        return jdbcTemplate.query("SELECT * FROM message WHERE room = ? ORDER BY datetime LIMIT 30", messageRowMapper, id);
    }

    @Override
    public List<Message> findAll() {
        return jdbcTemplate.query("SELECT * FROM message", messageRowMapper);
    }

    @Override
    public int save(Message entity) {
        return jdbcTemplate.update("INSERT INTO message (author, room, \"text\", datetime) VALUES (?, ?, ?, ?)", entity.getSender(), entity.getRoom(), entity.getText(), entity.getTimestamp());
    }

    @Override
    public void update(Message entity) {
        jdbcTemplate.update("UPDATE message SET \"text\" = ? WHERE (author = ? AND datetime = ?) OR id = ?",
                entity.getText(), entity.getSender(), entity.getTimestamp(), entity.getId());
    }

    @Override
    public void delete(String name) {
        jdbcTemplate.update("DELETE FROM message WHERE author = ?", name);
    }

    public static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Message(rs.getLong("author"), rs.getLong("room"), rs.getString("text"), rs.getTimestamp("dateTime"));
        }
    }
}
