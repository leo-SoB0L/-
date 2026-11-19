package ru.mirea.magic.gamelogic;

import java.io.IOException;

public interface GameEngine {

    /**
     * Метод для запуска игровой последовательности
     */
    void startGameLoop() throws IOException;
}
