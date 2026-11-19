package ru.mirea.magic.card;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class Ability implements AbilityResolving, Serializable {

    /**
     * Небольшой "хак" чтобы объекты способностей из разных проектов имели одинаковую версию
     * <a href="https://javarush.com/groups/posts/1034-zachem-ispoljhzovatjh-serialversionuid-vnutri-serializable-klassa-v-java">Как это работает</a>
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private final String abilityName;

    public String getAbilityName() {
        return abilityName;
    }

    /**
     * Используем для сравнения объектов способностей между собой
     * @param o объект Ability
     * @return является ли входной объект той же способностью
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ability ability = (Ability) o;
        return Objects.equals(abilityName, ability.abilityName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(abilityName);
    }

    public Ability(String abilityName) {
        this.abilityName = abilityName;
    }
}
