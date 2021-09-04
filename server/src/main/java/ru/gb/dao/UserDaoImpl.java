package ru.gb.dao;

import ru.gb.PooledDataSource;
import ru.gb.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final String TABLE_NAME = "usr";
    private static final String COL_ID = "id";
    private static final String COL_LOGIN = "login";
    private static final String COL_PASSWORD = "password";
    private static final String COL_NICKNAME = "nickname";

    private final DataSource ds;
    private Connection connection;
    private PreparedStatement preStatement;

    public UserDaoImpl() {
        ds = PooledDataSource.getDataSource();
    }

    private void disconnect() {
        if (preStatement != null) {
            try {
                preStatement.close();
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

    @Override
    public Optional<User> findById(int id) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COL_ID));
            preStatement.setInt(1, id);
            ResultSet resultSet = preStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(
                        new User(
                                resultSet.getInt(COL_ID),
                                resultSet.getString(COL_LOGIN),
                                resultSet.getString(COL_PASSWORD),
                                resultSet.getString(COL_NICKNAME)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = null;
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format("SELECT * FROM %s", TABLE_NAME));
            ResultSet resultSet = preStatement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt(COL_ID),
                        resultSet.getString(COL_LOGIN),
                        resultSet.getString(COL_PASSWORD),
                        resultSet.getString(COL_NICKNAME)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return users;
    }

    @Override
    public boolean save(User user) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format(
                            "INSERT INTO %s " +
                                    "(%s, %s, %s) " +
                                    "VALUES (?, ?, ?) ",
                            TABLE_NAME,
                            COL_LOGIN, COL_PASSWORD, COL_NICKNAME
                    ));
            preStatement.setString(1, user.getLogin());
            preStatement.setString(2, user.getPassword());
            preStatement.setString(3, user.getNickname());
            preStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect();
        }
    }

    @Override
    public void update(User user) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format(
                            "UPDATE %s SET %s = ?, %s = ?, %s = ? WHERE %s = ?",
                            TABLE_NAME,
                            COL_LOGIN, COL_PASSWORD, COL_NICKNAME,
                            COL_ID
                    ));
            preStatement.setString(1, user.getLogin());
            preStatement.setString(2, user.getPassword());
            preStatement.setString(3, user.getNickname());
            preStatement.setInt(4, user.getId());
            preStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    @Override
    public void delete(User user) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format("DELETE FROM %s WHERE %s = ?", TABLE_NAME, COL_ID));
            preStatement.setInt(1, user.getId());
            preStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COL_LOGIN));
            preStatement.setString(1, login);
            ResultSet resultSet = preStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(
                        new User(
                                resultSet.getInt(COL_ID),
                                resultSet.getString(COL_LOGIN),
                                resultSet.getString(COL_PASSWORD),
                                resultSet.getString(COL_NICKNAME)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByLoginAndPassword(String login, String password) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format(
                            "SELECT * FROM %s WHERE %s = ? AND %s = ?",
                            TABLE_NAME, COL_LOGIN, COL_PASSWORD)
            );
            preStatement.setString(1, login);
            preStatement.setString(2, password);
            ResultSet resultSet = preStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(
                        new User(
                                resultSet.getInt(COL_ID),
                                resultSet.getString(COL_LOGIN),
                                resultSet.getString(COL_PASSWORD),
                                resultSet.getString(COL_NICKNAME)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        try {
            connection = ds.getConnection();
            preStatement = connection.prepareStatement(
                    String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COL_NICKNAME));
            preStatement.setString(1, nickname);
            ResultSet resultSet = preStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(
                        new User(
                                resultSet.getInt(COL_ID),
                                resultSet.getString(COL_LOGIN),
                                resultSet.getString(COL_PASSWORD),
                                resultSet.getString(COL_NICKNAME)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return Optional.empty();
    }
}
