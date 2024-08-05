package edu.school21.sockets.repositories;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {
    List<T> findAll();

    int save(T entity);

    void update(T entity);

    void delete(String name);
}
