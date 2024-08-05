package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component("roomsRepository")
public class RoomsRepositoryImpl implements RoomsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RoomRowMapper roomRowMapper;

    @Autowired
    public RoomsRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.roomRowMapper = new RoomRowMapper();
    }

    @Override
    public Long findCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chatroom", Long.class);
    }

    @Override
    public Optional<Room> findByName(String name) {
        return jdbcTemplate.query("SELECT * FROM chatroom WHERE name = ?", roomRowMapper, name).stream().findFirst();
    }

    @Override
    public List<Room> findAll() {
        return jdbcTemplate.query("SELECT * FROM chatroom", roomRowMapper);
    }

    @Override
    public int save(Room entity) {
        return jdbcTemplate.update("INSERT INTO chatroom (name, owner) VALUES (?, ?)", entity.getName(), entity.getOwner());
    }

    @Override
    public void update(Room entity) {
        jdbcTemplate.update("UPDATE chatroom SET name = ?, owner = ? WHERE id = ?", entity.getName(), entity.getOwner(), entity.getId());
    }

    @Override
    public void delete(String name) {
        jdbcTemplate.update("DELETE FROM chatroom WHERE name = ?", name);
    }

    public static class RoomRowMapper implements RowMapper<Room> {
        @Override
        public Room mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Room(rs.getLong("id"), rs.getString("name"), rs.getLong("owner"));
        }
    }
}
