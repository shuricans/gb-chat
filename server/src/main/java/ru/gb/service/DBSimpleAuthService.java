package ru.gb.service;

import ru.gb.dao.UserDao;
import ru.gb.dao.UserDaoImpl;
import ru.gb.model.User;

public class DBSimpleAuthService implements AuthService {

    private final UserDao userDao;

    public DBSimpleAuthService() {
        userDao = new UserDaoImpl();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {

        User user = userDao.findByLoginAndPassword(login, password).orElse(null);
        if (user != null) {
            return user.getNickname();
        }
        return null;
    }
}
