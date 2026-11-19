package ru.mirea.magic.group49;

import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.BattleSystem;
import ru.mirea.magic.gamelogic.GameDisplayContext;
import ru.mirea.magic.gamelogic.GameEngine;
import ru.mirea.magic.gamelogic.GameIO;

import java.io.IOException;

/**
 * Игровая последовательность: загружаем двух игроков из ресурсов и проводим матч.
 * Ввод с клавиатуры не используется, поэтому цикл можно запускать и в автоматических тестах.
 */
public class SimpleGameEngine implements GameEngine {

    private static final String PLAYER_ONE = "src/main/resources/players/player_jet.txt";
    private static final String PLAYER_TWO = "src/main/resources/players/player_opponent.txt";

    private final GameIO gameIO;
    private final BattleSystem battleSystem;
    private final GameDisplayContext display;

    public SimpleGameEngine(GameIO gameIO, BattleSystem battleSystem, GameDisplayContext display) {
        this.gameIO = gameIO;
        this.battleSystem = battleSystem;
        this.display = display;
    }

    @Override
    public void startGameLoop() throws IOException {
        display.showCurrentPhase("Загрузка игроков");
        Student playerOne = gameIO.playerLoad(PLAYER_ONE);
        Student playerTwo = gameIO.playerLoad(PLAYER_TWO);

        display.displayText(playerOne.getFirstName() + " (" + playerOne.getCard().getName() + ") против "
                + playerTwo.getFirstName() + " (" + playerTwo.getCard().getName() + ")");

        battleSystem.prepareMatch(playerOne, playerTwo);
        battleSystem.startBattle();
    }
}
