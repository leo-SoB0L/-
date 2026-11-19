package ru.mirea.magic.gamelogic;

import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.Student;

/**
 * Интерфейс для вывода игровых данных и сообщений на экран. Может быть реализован любым способом: консольный вывод, GUI вывод.
 * Основной метод для реализации:
 * {@snippet lang=java :
 *      void displayText(String text);
 * }
 * В нём необходимо определить конкретный способ вывода текста на экран или интерфейс, при необходимости совершать предобработку текста цветом и форматированием.
 * Подразумевается, что все остальные методы будут вызывать непосредственно {@code displayText()}
 */
public interface GameDisplayContext {

    /**
     * Вывести на экран данные о повреждении существ
     * @param attacker атакующее существо
     * @param target целевое существо
     */
    void showDamageTarget(CreatureCard attacker, CreatureCard target);

    /**
     * Вывести игрока - победителя
     * @param winner игрок, победивший в бою
     */
    void showPlayerWinner(Student winner);


    /**
     * Вывести текущую фазу боя
     * @param phase название фазы боя
     */
    void showCurrentPhase(String phase);

    /**
     * Вывод текста на экран.
     * Может реализовывать отображение через стандартный вывод на экран {@code System.out.println()}.
     * @param text текст
     */
    void displayText(String text);
}
