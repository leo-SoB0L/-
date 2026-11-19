package ru.mirea.magic.group49;

import ru.mirea.magic.abilities.FirstStrikeAbility;
import ru.mirea.magic.card.Ability;
import ru.mirea.magic.card.Attacker;
import ru.mirea.magic.card.Blocker;
import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.gamelogic.BattleContext;

import java.util.List;

/**
 * Реализация фаз боя. Контекст ничего не хранит: все карты приходят в параметрах методов.
 */
public class SimpleBattleContext implements BattleContext {

    @Override
    public void dealDamagePhase(Attacker attacker, CreatureCard target) {
        attacker.attack(target);
    }

    @Override
    public void blockDamagePhase(Blocker blocker, CreatureCard target) {
        blocker.block(target);
    }

    @Override
    public CreatureCard checkToughnessPhase(List<CreatureCard> cardList) {
        CreatureCard survivor = null;
        int alive = 0;
        for (CreatureCard card : cardList) {
            if (card.getToughness() > 0) {
                alive++;
                survivor = card;
            }
        }
        // победитель есть, только если выжило ровно одно существо из нескольких
        if (alive == 1 && cardList.size() > 1) {
            return survivor;
        }
        return null;
    }

    @Override
    public void resolveFirstStrikeAbilityPhase(List<CreatureCard> creatures) {
        long withFirstStrike = creatures.stream()
                .filter(card -> card.hasAbility(FirstStrikeAbility.class))
                .count();

        // первый удар у всех или ни у кого - преимущества нет
        if (withFirstStrike == 0 || withFirstStrike == creatures.size()) {
            return;
        }

        for (CreatureCard striker : creatures) {
            if (!striker.hasAbility(FirstStrikeAbility.class)) {
                continue;
            }
            Ability firstStrike = striker.getAbility(FirstStrikeAbility.class);
            for (CreatureCard other : creatures) {
                if (other != striker && striker.getToughness() > 0 && other.getToughness() > 0) {
                    firstStrike.resolve(striker, other);
                    if (striker.getPower() > 0) {
                        resolveAfterDealDamageAbilityPhase(striker, other);
                    }
                }
            }
        }
    }

    @Override
    public void resolveAfterDealDamageAbilityPhase(CreatureCard damager, CreatureCard target) {
        if (damager.getPower() <= 0) {
            return;
        }
        for (Ability ability : damager.getAbilities()) {
            // первый удар уже сработал в своей фазе, повторно его не применяем
            if (ability instanceof FirstStrikeAbility) {
                continue;
            }
            ability.resolve(damager, target);
        }
    }
}
