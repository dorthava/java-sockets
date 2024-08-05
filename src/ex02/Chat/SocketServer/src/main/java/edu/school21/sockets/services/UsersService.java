package edu.school21.sockets.services;

import edu.school21.sockets.models.User;

import java.util.Optional;

public interface UsersService {
    boolean signUp(String name, String password);
    boolean signIn(String name, String password);
    Optional<User> findByName(String name);
    Optional<User> findById(Long id);
}
