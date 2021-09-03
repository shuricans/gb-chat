package ru.gb;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

import static ru.gb.ConfigProperties.getPropertyValue;

public class PooledDataSource {

    private static final BasicDataSource BD_SRC;

    static {
        BD_SRC = new BasicDataSource();
        BD_SRC.setDriverClassName(getPropertyValue("driverClass"));
        BD_SRC.setUrl(getPropertyValue("url"));
        BD_SRC.setUsername(getPropertyValue("username"));
        BD_SRC.setPassword(getPropertyValue("password"));

        // Parameters for connection pooling
        BD_SRC.setInitialSize(10);
        BD_SRC.setMaxTotal(10);
    }

    public static DataSource getDataSource() {
        return BD_SRC;
    }
}