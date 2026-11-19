package ru.mirea.magic.group49;

/** Запуск локального матча с ресурсами группы. */
public final class Group49Launcher {
    private Group49Launcher() {}

    public static void main(String[] args) throws Exception {
        var display = new ConsoleDisplayContext();
        var context = new SimpleBattleContext();
        var battle = new SimpleBattleSystem(context, display);
        new SimpleGameEngine(new FileGameIO(), battle, display).startGameLoop();
    }
}
