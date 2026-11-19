package ru.mirea.magic.gamelogic;

import ru.mirea.magic.card.Student;

import java.sql.SQLException;
import java.util.UUID;

public interface DatabaseService {

    Student findStudentByID(UUID id) throws SQLException;

    Student findStudentByUsername(String username) throws SQLException;

    UUID saveStudent(Student student) throws SQLException;

}
