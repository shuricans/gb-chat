package ru.gb.dao;

import ru.gb.model.User;

import java.util.Optional;

public interface UserDao extends Dao<User> {

    Optional<User> findByLogin(String login);
    Optional<User> findByLoginAndPassword(String login, String password);
    Optional<User> findByNickname(String nickname);
}
