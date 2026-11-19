package ru.mirea.magic.gamelogic;

import ru.mirea.magic.card.Student;

import java.io.IOException;

public interface NetClient {

    void connectToServer(String address, Student player) throws IOException;
}
