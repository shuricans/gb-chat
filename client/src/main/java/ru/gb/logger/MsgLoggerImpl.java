package ru.gb.logger;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class MsgLoggerImpl implements MsgLogger {

    public static final String NEW_LINE = System.lineSeparator();

    private final Path path;

    public MsgLoggerImpl(Path path) {
        this.path = path;
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(String msg) {
        try {
            Files.writeString(
                    path,
                    msg + NEW_LINE,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getLastRows(int countRows) {
        LinkedList<String> result = null;
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(path.toFile(), StandardCharsets.UTF_8)) {
            result = new LinkedList<>();
            int count = 0;
            while (count < countRows) {
                String s = reader.readLine();
                if (s != null) {
                    result.addFirst(s);
                    count++;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
