package ru.mirea.magic.group49;

import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.DatabaseService;
import ru.mirea.magic.gamelogic.GameIO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Заготовка доступа к базе данных игроков.
 *
 * <p>Параметры подключения читаются из файла src/main/resources/db.properties
 * (ключи url, user, password) через {@link GameIO#resourceLoad(String)}.
 * Пока этого файла нет, методы выбрасывают SQLException с понятным сообщением.
 *
 * <p>TODO: после получения от преподавателя адреса базы, схемы таблиц и драйвера JDBC
 * реализовать запросы в методах ниже.
 */
public class JdbcDatabaseService implements DatabaseService {

    private final GameIO gameIO;

    public JdbcDatabaseService(GameIO gameIO) {
        this.gameIO = gameIO;
    }

    @Override
    public Student findStudentByID(UUID id) throws SQLException {
        try (Connection connection = openConnection()) {
            // TODO: SELECT игрока и его карты по id
            throw new SQLException("Поиск по UUID ещё не реализован");
        }
    }

    @Override
    public Student findStudentByUsername(String username) throws SQLException {
        try (Connection connection = openConnection()) {
            // TODO: SELECT игрока и его карты по username
            throw new SQLException("Поиск по username ещё не реализован");
        }
    }

    @Override
    public UUID saveStudent(Student student) throws SQLException {
        try (Connection connection = openConnection()) {
            // TODO: INSERT игрока и его карты, вернуть сгенерированный UUID
            throw new SQLException("Сохранение игрока ещё не реализовано");
        }
    }

    private Connection openConnection() throws SQLException {
        ResourceBundle settings;
        try {
            settings = gameIO.resourceLoad("db");
        } catch (MissingResourceException e) {
            throw new SQLException("База данных не настроена: нет файла src/main/resources/db.properties", e);
        }
        return DriverManager.getConnection(
                settings.getString("url"),
                settings.getString("user"),
                settings.getString("password"));
    }
}
