package edu.school21.sockets.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import edu.school21.sockets.models.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component("usersRepository")
public class UsersRepositoryImpl implements UsersRepository {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Autowired
    public UsersRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userRowMapper = new UserRowMapper();
    }

    @Override
    public Optional<User> findByName(String name) {
        List<User> users = jdbcTemplate.query("SELECT * FROM \"user\" WHERE name = ?", userRowMapper, name);
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM \"user\" WHERE id = ?", userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM \"user\"", userRowMapper);
    }

    @Override
    public int save(User entity) {
        return jdbcTemplate.update("INSERT INTO \"user\" (name, password) VALUES (?, ?)", entity.getName(), entity.getPassword());
    }

    @Override
    public void update(User entity) {
        jdbcTemplate.update("UPDATE \"user\" SET name = ?, password = ? WHERE id = ?", entity.getName(), entity.getPassword(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", id);
    }

    public static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(rs.getLong("id"), rs.getString("name"), rs.getString("password"));
        }
    }

}
