package ru.mirea.magic.group49;

import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.GameDisplayContext;

/**
 * Вывод игровых сообщений в консоль. Все методы вызывают {@link #displayText(String)}.
 */
public class ConsoleDisplayContext implements GameDisplayContext {

    @Override
    public void showDamageTarget(CreatureCard attacker, CreatureCard target) {
        displayText("%s (сила %d) наносит удар существу %s. Здоровье %s: %d".formatted(
                attacker.getName(), attacker.getPower(), target.getName(),
                target.getName(), target.getToughness()));
    }

    @Override
    public void showPlayerWinner(Student winner) {
        if (winner == null) {
            displayText("Ничья!");
            return;
        }
        displayText("Победитель: %s %s, карта %s".formatted(
                winner.getFirstName(), winner.getLastName(), winner.getCard().getName()));
    }

    @Override
    public void showCurrentPhase(String phase) {
        displayText("=== " + phase + " ===");
    }

    @Override
    public void displayText(String text) {
        System.out.println(text);
    }
}
