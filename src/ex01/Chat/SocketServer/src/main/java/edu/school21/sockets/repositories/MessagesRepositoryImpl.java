package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component("messagesRepository")
public class MessagesRepositoryImpl implements MessagesRepository {
    private final JdbcTemplate jdbcTemplate;
    private final  MessageRowMapper messageRowMapper;

    @Autowired
    public MessagesRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.messageRowMapper = new MessageRowMapper();
    }

    @Override
    public Optional<Message> findByTimestamp(Timestamp timestamp) {
        List<Message> messages = jdbcTemplate.query("SELECT * FROM message WHERE \"timestamp\" = ?", messageRowMapper, timestamp);
        return messages.stream().findFirst();
    }

    @Override
    public List<Message> findAll() {
        return jdbcTemplate.query("SELECT * FROM message", messageRowMapper);
    }

    @Override
    public int save(Message entity) {
        return jdbcTemplate.update("INSERT INTO message (sender, \"text\", \"timestamp\") VALUES (?, ?, ?)", entity.getSender(), entity.getText(), entity.getTimestamp());
    }

    @Override
    public void update(Message entity) {
        jdbcTemplate.update("UPDATE message SET \"text\" = ? WHERE (sender = ? AND \"timestamp\" = ?)",
                entity.getText(), entity.getSender(), entity.getTimestamp());
    }

    @Override
    public void delete(String name) {
        jdbcTemplate.update("DELETE FROM message WHERE sender = ?", name);
    }

    public static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Message(rs.getString("sender"), rs.getString("text"), rs.getTimestamp("timestamp"));
        }
    }
}
