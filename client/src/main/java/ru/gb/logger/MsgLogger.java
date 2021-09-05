package ru.gb.logger;

import java.util.List;

public interface MsgLogger {
    void write(String msg);
    List<String> getLastRows(int countRows);
}
