package ru.gb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

    private static final String FILE_CONFIG_PROP = "config.properties";
    private static Properties prop = new Properties();

    static {
        try (InputStream input = ConfigProperties.class.getClassLoader().getResourceAsStream(FILE_CONFIG_PROP)) {

            if (input == null) {
                throw new FileNotFoundException(String.format("Unable to find \"%s\"", FILE_CONFIG_PROP));
            }

            prop = new Properties();
            prop.load(input);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPropertyValue(String propertyKey) {
        if (propertyKey != null && !propertyKey.isEmpty()) {
            return prop.getProperty(propertyKey);
        }
        return null;
    }

}
