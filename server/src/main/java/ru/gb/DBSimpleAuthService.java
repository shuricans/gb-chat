package ru.gb;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBSimpleAuthService implements AuthService {

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {

        final String selectSQL = "SELECT nickname FROM usr WHERE login = ? AND password = ?";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String nickname = null;

        try {
            final DataSource ds = PooledDataSource.getDataSource();
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(selectSQL);
            prepareStatement.setString(1, login);
            prepareStatement.setString(2, password);
            ResultSet rs = prepareStatement.executeQuery();
            if (rs.next()) {
                nickname = rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (prepareStatement != null) {
                try {
                    prepareStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return nickname;
    }
}
