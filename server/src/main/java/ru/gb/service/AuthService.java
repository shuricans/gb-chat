package ru.gb.service;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
}
