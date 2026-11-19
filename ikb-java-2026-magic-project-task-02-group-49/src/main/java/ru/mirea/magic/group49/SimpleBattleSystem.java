package ru.mirea.magic.group49;

import ru.mirea.magic.card.Card;
import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.BattleContext;
import ru.mirea.magic.gamelogic.BattleSystem;
import ru.mirea.magic.gamelogic.GameDisplayContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Матч двух игроков. В бою участвуют копии карт, поэтому исходные карты игроков не повреждаются
 * (это важно для турнира, где победитель играет дальше).
 */
public class SimpleBattleSystem implements BattleSystem {

    private final BattleContext battleContext;
    private final GameDisplayContext display;

    /** Ключ - игрок, значение - его карта (копия) в текущем матче. */
    private final Map<Student, CreatureCard> playerCard = new LinkedHashMap<>();

    public SimpleBattleSystem(BattleContext battleContext, GameDisplayContext display) {
        this.battleContext = battleContext;
        this.display = display;
    }

    @Override
    public void prepareMatch(Student playerOne, Student playerTwo) {
        if (playerOne == null || playerTwo == null) {
            throw new IllegalArgumentException("Игроки не должны быть null");
        }
        if (playerOne == playerTwo) {
            throw new IllegalArgumentException("Игрок не может играть сам с собой");
        }
        CreatureCard first = creatureOf(playerOne);
        CreatureCard second = creatureOf(playerTwo);
        playerCard.clear();
        playerCard.put(playerOne, first);
        playerCard.put(playerTwo, second);
    }

    @Override
    public Student startBattle() {
        if (playerCard.size() != 2) {
            throw new IllegalStateException("Матч не подготовлен: сначала вызовите prepareMatch");
        }

        List<Student> students = new ArrayList<>(playerCard.keySet());
        List<CreatureCard> cards = new ArrayList<>(playerCard.values());

        display.showCurrentPhase("Первый удар");
        battleContext.resolveFirstStrikeAbilityPhase(cards);

        int round = 0;
        int firstAtCycleStart = cards.get(0).getToughness();
        int secondAtCycleStart = cards.get(1).getToughness();
        while (allAlive(cards)) {
            // игроки атакуют по очереди
            CreatureCard attacker = cards.get(round % 2);
            CreatureCard target = cards.get((round + 1) % 2);

            display.showCurrentPhase("Раунд " + (round + 1) + ": атака");
            battleContext.dealDamagePhase(attacker, target);
            battleContext.resolveAfterDealDamageAbilityPhase(attacker, target);
            display.showDamageTarget(attacker, target);

            display.showCurrentPhase("Раунд " + (round + 1) + ": блок");
            battleContext.blockDamagePhase(target, attacker);
            battleContext.resolveAfterDealDamageAbilityPhase(target, attacker);
            display.showDamageTarget(target, attacker);

            display.showCurrentPhase("Раунд " + (round + 1) + ": проверка здоровья");
            battleContext.checkToughnessPhase(cards);
            round++;
            // После двух раундов порядок атак повторяется. Если ни одна карта
            // не потеряла здоровье, продолжение не приведёт к победителю.
            if (round % 2 == 0 && allAlive(cards)) {
                int firstNow = cards.get(0).getToughness();
                int secondNow = cards.get(1).getToughness();
                if (firstNow >= firstAtCycleStart && secondNow >= secondAtCycleStart) {
                    break;
                }
                firstAtCycleStart = firstNow;
                secondAtCycleStart = secondNow;
            }
        }

        CreatureCard winnerCard = battleContext.checkToughnessPhase(cards);
        Student winner = winnerCard == null ? null : students.get(cards.indexOf(winnerCard));
        display.showPlayerWinner(winner);
        return winner;
    }

    private boolean allAlive(List<CreatureCard> cards) {
        return cards.stream().allMatch(card -> card.getToughness() > 0);
    }

    private CreatureCard creatureOf(Student student) {
        Card card = student.getCard();
        if (!(card instanceof CreatureCard creature)) {
            throw new IllegalArgumentException(
                    "У игрока " + student.getFirstName() + " нет карты существа");
        }
        return copyOf(creature);
    }

    /** Глубокая копия карты через сериализацию. */
    private CreatureCard copyOf(CreatureCard card) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
                out.writeObject(card);
            }
            try (ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bytes.toByteArray()))) {
                return (CreatureCard) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Не удалось скопировать карту " + card.getName(), e);
        }
    }
}
