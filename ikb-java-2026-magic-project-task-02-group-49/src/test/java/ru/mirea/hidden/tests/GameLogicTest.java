package ru.mirea.hidden.tests;

import org.junit.jupiter.api.*;
import ru.mirea.hidden.tests.ioc.ComponentContext;
import ru.mirea.hidden.tests.ioc.ComponentFactory;
import ru.mirea.hidden.tests.ioc.EngineConfiguration;
import ru.mirea.hidden.tests.ioc.JavaConfiguration;
import ru.mirea.magic.MagicLauncher;
import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.ManaType;
import ru.mirea.magic.card.Student;
import ru.mirea.magic.gamelogic.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Tag("game-logic-tests")
@DisplayName("Тестирование игрового функционала")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameLogicTest {
    private static ComponentContext componentContext;

    private static final Class<?>[] requiredImplementsArray = new Class[]{
            GameEngine.class,
            BattleSystem.class,
            BattleContext.class,
            GameIO.class,
            GameDisplayContext.class,
            GameServer.class,
            TournamentComponent.class,
            NetClient.class,
            DatabaseService.class
    };

    private static final List<String> groups = List.of(
            "БСБО-51-25",
            "БСБО-52-25",
            "БСБО-53-25",
            "БСБО-54-25",
            "БСБО-55-25",
            "БСБО-56-25",
            "БСБО-57-25",
            "БСБО-58-25",
            "Teacher"
    );

    @BeforeAll
    @DisplayName("Внедряем все зависимости из ru.mirea.magic")
    static void getUserPackageSubfolder() {
        System.out.println();
        System.out.println("===".repeat(50));
        System.out.println(">>Внедрение зависимостей<<");
        System.out.println("===".repeat(50));
        System.out.println();
        EngineConfiguration engineConfiguration = new JavaConfiguration(MagicLauncher.class.getPackageName(), new HashMap<>());
        componentContext = new ComponentContext(engineConfiguration);
        ComponentFactory factory = new ComponentFactory(componentContext);

        componentContext.setComponentFactory(factory);
        Assertions.assertDoesNotThrow(() -> {
            for(Class<?> ifc : requiredImplementsArray) {
                for (Class<?> aClass : componentContext
                        .getEngineConfiguration()
                        .getScanner()
                        .getSubTypesOf(ifc)) {

                    Class<?> anInterface = null;

                    if (aClass.getInterfaces().length > 0) {
                        anInterface = aClass.getInterfaces()[0];
                    }

                    Class<?> aKey = anInterface != null ? anInterface : aClass;

                    System.out.printf("🔄Инициализация компонента %s%n", aKey.getSimpleName());

                    Object componentInstance = factory.createComponent(aClass);

                    System.out.printf("✅Объект компонента найден, кладём в кэш %s%n", componentInstance);

                    componentContext.getCache().put(aKey, componentInstance);
                }
            }
        }, "❌ Произошла ошибка во время инициализации зависимостей");

        System.out.println();
        System.out.println("===".repeat(50));
        System.out.println("✅Внедрение зависимостей завершено");
        System.out.println("===".repeat(50));
        System.out.println();
    }

    @Test
    @DisplayName("Проверка наличия реализаций интерфейсов")
    @Order(1)
    void implementationsExists() {
        System.out.println(">>Проверка наличия реализаций интерфейсов<<");

        for (Class<?> ifc : requiredImplementsArray) {
            System.out.printf("Проверка: %s%n", ifc.getSimpleName());
            Assertions.assertTrue(componentContext.getCache().containsKey(ifc),
                    "❌ Реализация %s отсутствует".formatted(ifc.getSimpleName()));
            System.out.printf("✅Реализация %s найдена%n", ifc.getSimpleName());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Проверка создания карты из тестового файла")
    void createCardTest() throws IOException {
        System.out.println(">>Проверка создания карты из тестового файла<<");

        GameIO gameIO = componentContext.getComponent(GameIO.class);

        InputStream resourceAsStream = getClass().getResourceAsStream("/test-card.txt");

        if (Objects.isNull(resourceAsStream)) {
            Assertions.fail("Не найден файл test-card.txt");
        }

        CreatureCard creatureCard = gameIO.cardLoad("src/test/resources/test-card.txt");

        Assertions.assertNotNull(creatureCard, "❌ Загрузка карты возвращает null");

        Assertions.assertEquals("D'Avenant Healer", creatureCard.getName(), "❌ Некорректная загрузка карты");

        System.out.println("✅ Карта загружена корректно");
    }

    @Test
    @Order(3)
    @DisplayName("Тестирование контекста битвы")
    void battleContextTest() throws IOException {
        System.out.println(">>Тестирование контекста битвы<<");

        BattleContext battleContext = componentContext.getComponent(BattleContext.class);

        GameIO gameIO = componentContext.getComponent(GameIO.class);

        CreatureCard keenCard = gameIO.cardLoad("src/test/resources/keen.txt");

        CreatureCard davenantCard = gameIO.cardLoad("src/test/resources/test-card.txt");

        System.out.println(">>Проверка фазы нанесения урона<<");

        System.out.printf("""
                        Карты до нанесения урона
                        %s: %d/%d
                        %s: %d/%d
                        %n""", keenCard.getName(), keenCard.getPower(), keenCard.getToughness(),
                davenantCard.getName(), davenantCard.getPower(), davenantCard.getToughness());

        battleContext.dealDamagePhase(keenCard, davenantCard);

        System.out.printf("""
                        Карты после нанесения урона
                        %s: %d/%d
                        %s: %d/%d
                        %n""", keenCard.getName(), keenCard.getPower(), keenCard.getToughness(),
                davenantCard.getName(), davenantCard.getPower(), davenantCard.getToughness());

        Assertions.assertTrue(davenantCard.getToughness() <= 0);

        System.out.println("✅ Тест фазы пройден");

        System.out.println(">>Проверка фазы блокирования<<");

        System.out.printf("""
                        Карты до блокирования
                        %s: %d/%d
                        %s: %d/%d
                        %n""", keenCard.getName(), keenCard.getPower(), keenCard.getToughness(),
                davenantCard.getName(), davenantCard.getPower(), davenantCard.getToughness());

        battleContext.blockDamagePhase(davenantCard, keenCard);

        System.out.printf("""
                        Карты после блокирования
                        %s: %d/%d
                        %s: %d/%d
                        %n""", keenCard.getName(), keenCard.getPower(), keenCard.getToughness(),
                davenantCard.getName(), davenantCard.getPower(), davenantCard.getToughness());

        Assertions.assertTrue(keenCard.getToughness() <= 0);

        System.out.println("✅ Тест фазы пройден");

        System.out.println(">>Проверка фазы оценки здоровья<<");

        System.out.printf("""
                        Состояние карт
                        %s: %d/%d
                        %s: %d/%d
                        %n""", keenCard.getName(), keenCard.getPower(), keenCard.getToughness(),
                davenantCard.getName(), davenantCard.getPower(), davenantCard.getToughness());

        CreatureCard winnerCard = battleContext.checkToughnessPhase(List.of(keenCard, davenantCard));

        Assertions.assertNull(winnerCard, "❌ Должна быть ничья");

        System.out.println("✅ Тест фазы пройден");
    }

    @Test
    @Order(4)
    @DisplayName("Тестирование боевой системы")
    void battleSystemTest() throws IOException {

        System.out.println(">>Тестирование боевой системы<<");

        BattleSystem battleSystem = componentContext.getComponent(BattleSystem.class);

        GameIO gameIO = componentContext.getComponent(GameIO.class);

        Student playerOne = gameIO.playerLoad("src/test/resources/testPlayerOne.txt");

        Student playerTwo = gameIO.playerLoad("src/test/resources/testPlayerTwo.txt");

        Assertions.assertNotNull(playerOne, "❌ Загрузка игрока 1 возвращает null");

        Assertions.assertNotNull(playerTwo, "❌ Загрузка игрока 2 возвращает null");

        Assertions.assertNotNull(playerOne.getCard(), "❌ Карта игрока 1 не загружена");

        Assertions.assertNotNull(playerTwo.getCard(), "❌ Карта игрока 2 не загружена");

        System.out.println(">>Подготовка к матчу<<");

        battleSystem.prepareMatch(playerOne, playerTwo);

        System.out.println(">>Старт битвы<<");

        Student winner = battleSystem.startBattle();

        if (Objects.nonNull(winner)) {
            System.out.printf("Победитель %s с картой %s%n", winner.getFirstName(), winner.getCard().getName());
        } else {
            System.out.println("Ничья!");
        }

        System.out.println("✅ Тест боевой системы пройден");
    }

    @Test
    @Order(5)
    @DisplayName("Тестирование игрового цикла")
    void gameEngineLoopTest() {

        System.out.println(">>Тестирование игрового цикла<<");

        GameEngine gameEngine = componentContext.getComponent(GameEngine.class);

        System.out.println(">>Запуск игрового цикла<<");

        Assertions.assertDoesNotThrow(gameEngine::startGameLoop, "❌ произошла ошибка во время выполнения игрового цикла");
    }

    @Test
    @Order(6)
    @DisplayName("Тестирование методов InputCard, ExportCard из GameIO")
    void gameIOTest() throws IOException {
        System.out.println(">>Тестирование методов InputCard, ExportCard GameIO<<");

        GameIO gameIO = componentContext.getComponent(GameIO.class);

        CreatureCard creatureCard = gameIO.cardLoad("src/test/resources/test-card.txt");

        gameIO.cardExport(creatureCard);

        File serializedCard = new File(creatureCard.getName() + ".crd");
        Assertions.assertTrue(serializedCard.exists(), "❌ Ошибка, файл карты %s не найден".formatted(creatureCard.getName()));

        System.out.println("✅ Файл карты создан");

        try {
            CreatureCard card = gameIO.cardImport(creatureCard.getName() + ".crd");
            System.out.println("✅ Файл карты загружен, проверка полей");
            Assertions.assertTrue(allFieldsAreCorrect(card), "❌ Поля карты %s загружены некорректно %s".formatted(creatureCard.getName(), creatureCard.toString()));
        } catch (IOException e) {
            System.out.printf("Параметр названия карты %s с расширением .crd не сработал - пробуем другой вариант%n", creatureCard.getName());

            Assertions.assertDoesNotThrow(() -> {
                CreatureCard card = gameIO.cardImport(creatureCard.getName());
                System.out.println("✅ Файл карты загружен, проверка полей");
                Assertions.assertTrue(allFieldsAreCorrect(card), "❌ Поля карты %s загружены некорректно %s".formatted(creatureCard.getName(), creatureCard.toString()));
            }, "❌ произошла ошибка во время загрузки карты %s".formatted(creatureCard.getName()));
        }

        Assertions.assertDoesNotThrow(() -> {
            serializedCard.delete();
        }, "❌ произошла ошибка удаления карты");


        System.out.println("✅ Тест сериализации и десериализации пройден");
    }

    private boolean allFieldsAreCorrect(CreatureCard deserializedCard) {
        return deserializedCard.getName().equals("D'Avenant Healer") &&
                deserializedCard.getPower() == 1 &&
                deserializedCard.getToughness() == 2 &&
                deserializedCard.getManaCost().getGenericMana() == 1 &&
                deserializedCard.getManaCost().getColoredMana().containsKey(ManaType.WHITE) &&
                deserializedCard.getManaCost().getColoredMana().get(ManaType.WHITE) == 2 &&
                deserializedCard.getAbilities().isEmpty() &&
                deserializedCard.getOracleText().isEmpty();
    }

    @Test
    @DisplayName("Тестирование турнирной системы")
    @Order(7)
    void tournamentLocalTest() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        System.out.println(">>Тестирование турнирной системы<<");

        TournamentComponent tournamentComponent = componentContext.getComponent(TournamentComponent.class);

        GameIO gameIO = componentContext.getComponent(GameIO.class);

        List<Student> players = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            Student player = gameIO.playerLoad("src/test/resources/player%d.txt".formatted(i));
            players.add(player);
        }

        Assertions.assertDoesNotThrow(() -> {
            Student winner = tournamentComponent.startTournamentLocal(players);

            Assertions.assertNotNull(winner, "❌ Турнир не должен закончиться ничьей!");

        }, "❌ произошла ошибка во время проведения турнира");

        System.out.println("✅ Тест турнира пройден");
    }

    @Test
    @DisplayName("Тестирование создания и подключения к серверу")
    @Order(8)
    void createServerTest() {
        System.out.println(">>Тестирование создания и подключения к серверу<<");

        GameIO gameIO = componentContext.getComponent(GameIO.class);

        GameServer gameServer = componentContext.getComponent(GameServer.class);

        try (ExecutorService service = Executors.newSingleThreadExecutor()) {

            CompletableFuture<Void> async = CompletableFuture.runAsync(() -> {
                try {
                    gameServer.startServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<Void> async1 = CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(1500);

                    NetClient netClient = componentContext.getComponent(NetClient.class);

                    Student student = gameIO.playerLoad("src/test/resources/player1.txt");

                    System.out.printf("Игрок %s%n", student);

                    Assertions.assertDoesNotThrow(() -> {
                        System.out.println(">>Подключение к серверу<<");
                        netClient.connectToServer("127.0.0.1", student);
                    }, "❌ Ошибка при подключении к серверу");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            TimeUnit.MILLISECONDS.sleep(2500);

            async.cancel(true);
            async1.cancel(true);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("✅Проверка клиент-серверного взаимодействия пройдена");
    }

    @Test
    @DisplayName("Тестирование базы данных")
    @Order(9)
    void databaseServiceTest() {
        System.out.println(">>Тестирование базы данных<<");
        DatabaseService databaseService = componentContext.getComponent(DatabaseService.class);

        Assertions.assertDoesNotThrow(() -> {
            Student kseno = databaseService.findStudentByUsername("kseno");

            Assertions.assertNotNull(kseno, "❌ Объект игрока kseno не должен вернуть null");

        }, "❌ Ошибка при подключении к базе данных");
    }

    @Test
    @DisplayName("Поиск студентов и их карточек по UUID")
    @Order(10)
    void findStudentsByUuidFileTest() throws IOException, SQLException, NoSuchFieldException, IllegalAccessException {
        System.out.println(">>Тестирование наличия студентов в базе<<");

        File uuids = new File("uuids.txt");

        Assertions.assertTrue(uuids.exists(), "❌ Файл uuids.txt отсутствует!");

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(uuids))) {
            DatabaseService databaseService = componentContext.getComponent(DatabaseService.class);

            List<String> uuidsList = bufferedReader.lines().toList();

            Assertions.assertFalse(uuidsList.isEmpty(), "❌uuid.txt пуст!");

            for (String usernameIdLine : uuidsList) {
                String[] usernameId = usernameIdLine.split("=");

                String username = usernameId[0];
                UUID uuid = UUID.fromString(usernameId[1]);

                Student student = databaseService.findStudentByID(uuid);

                Assertions.assertNotNull(student, "❌ Игрок %s не найден в базе данных по UUID %s".formatted(
                        username,
                        uuid));

                Field field = student.getClass().getDeclaredField("username");
                field.setAccessible(true);
                String studentUsername = (String) field.get(student);

                System.out.printf("Получили объект игрока: %s%n", student);

                Assertions.assertEquals(username, studentUsername, "❌ Никнейм игрока %s из базы не совпадает с никнеймом из uuids.txt %s!".formatted(studentUsername, username
                ));

                Assertions.assertFalse(student.getGroup().isEmpty(), "❌Группа игрока должна быть загружена из базы");

                Assertions.assertThrows(NumberFormatException.class, () -> {
                    Integer.parseInt(student.getGroup());
                }, "❌Группа должна быть названием, а не идентификатором");

                String studentGroup = student.getGroup();

                Assertions.assertTrue(groups.contains(studentGroup), "❌Группа игрока должна совпадать с существующей");

                Assertions.assertNotNull(student.getCard(),"❌ Метод получения игрока из базы должен загружать и его карту" );

            }
        }
        System.out.println("✅Проверка uuids пройдена");
    }
}