package ru.mirea.magic.group49;

import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.BattleSystem;
import ru.mirea.magic.gamelogic.GameDisplayContext;
import ru.mirea.magic.gamelogic.TournamentComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Турнир на выбывание: игроки сражаются парами, победители проходят дальше.
 * Если в паре случилась ничья, побеждает игрок с более сильной картой.
 */
public class LocalTournamentComponent implements TournamentComponent {

    private final BattleSystem battleSystem;
    private final GameDisplayContext display;

    public LocalTournamentComponent(BattleSystem battleSystem, GameDisplayContext display) {
        this.battleSystem = battleSystem;
        this.display = display;
    }

    @Override
    public Student startTournamentLocal(List<Student> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Для турнира нужен хотя бы один игрок");
        }

        List<Student> current = new ArrayList<>(players);
        int stage = 1;
        while (current.size() > 1) {
            display.showCurrentPhase("Турнир, раунд " + stage);
            List<Student> next = new ArrayList<>();

            for (int i = 0; i + 1 < current.size(); i += 2) {
                next.add(playMatch(current.get(i), current.get(i + 1)));
            }
            if (current.size() % 2 == 1) {
                // нечётный игрок проходит дальше без боя
                next.add(current.getLast());
            }
            current = next;
            stage++;
        }

        Student champion = current.getFirst();
        display.displayText("Чемпион турнира: " + champion.getFirstName());
        return champion;
    }

    private Student playMatch(Student first, Student second) {
        battleSystem.prepareMatch(first, second);
        Student winner = battleSystem.startBattle();
        return winner != null ? winner : tieBreak(first, second);
    }

    /** При ничьей побеждает карта с большей суммой силы и выносливости, при равенстве - первый игрок. */
    private Student tieBreak(Student first, Student second) {
        return strength(second) > strength(first) ? second : first;
    }

    private int strength(Student student) {
        CreatureCard card = (CreatureCard) student.getCard();
        return card.getPower() + card.getToughness();
    }
}
