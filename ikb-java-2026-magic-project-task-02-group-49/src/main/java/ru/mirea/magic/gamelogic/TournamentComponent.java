package ru.mirea.magic.gamelogic;

import ru.mirea.magic.card.Student;

import java.util.List;

public interface TournamentComponent {

    // Уберите комментарий после того, как создадите класс ClientHandler
    //Student startTournamentNet(List<Student> players, List<ClientHandler> clientHandlers);

    Student startTournamentLocal(List<Student> players);

}
