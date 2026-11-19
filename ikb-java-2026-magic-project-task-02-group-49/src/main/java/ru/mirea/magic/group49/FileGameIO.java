package ru.mirea.magic.group49;

import ru.mirea.magic.abilities.DeathtouchAbility;
import ru.mirea.magic.abilities.FirstStrikeAbility;
import ru.mirea.magic.abilities.FlyingAbility;
import ru.mirea.magic.abilities.HasteAbility;
import ru.mirea.magic.abilities.LifelinkAbility;
import ru.mirea.magic.abilities.TrampleAbility;
import ru.mirea.magic.abilities.VigilanceAbility;
import ru.mirea.magic.card.Ability;
import ru.mirea.magic.card.CardType;
import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.ManaCost;
import ru.mirea.magic.card.ManaType;
import ru.mirea.magic.card.Student;
import ru.mirea.magic.card.SuperType;
import ru.mirea.magic.gamelogic.GameIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Загрузка и сохранение игровых данных: карты и игроки читаются из текстовых файлов,
 * карты можно сериализовать в файлы .crd.
 *
 * <p>Формат файла карты (7 строк):
 * название, мана-стоимость {1}{W}{W}, строка типа "Legendary Creature — Vampire Elf Rogue",
 * способности через запятую ("-" если нет), текст карты ("-" если нет), сила/выносливость, набор.
 *
 * <p>Формат файла игрока (4 строки): фамилия, имя, группа, путь к файлу карты без расширения .txt.
 */
public class FileGameIO implements GameIO {

    private static final Pattern MANA_SYMBOL = Pattern.compile("\\{([^}]*)}");
    private static final String MAIN_RESOURCES = "src/main/resources/";
    private static final String TEST_RESOURCES = "src/test/resources/";

    private static final Map<String, Function<String, Ability>> ABILITIES = Map.of(
            "deathtouch", DeathtouchAbility::new,
            "firststrike", FirstStrikeAbility::new,
            "flying", FlyingAbility::new,
            "haste", HasteAbility::new,
            "lifelink", LifelinkAbility::new,
            "trample", TrampleAbility::new,
            "vigilance", VigilanceAbility::new
    );

    @Override
    public ResourceBundle resourceLoad(String resourceName) {
        return ResourceBundle.getBundle(resourceName);
    }

    @Override
    public Student playerLoad(String playerName) throws IOException {
        String path = playerName.endsWith(".txt") ? playerName : playerName + ".txt";
        List<String> lines = readNonEmptyLines(path);
        if (lines.size() < 4) {
            throw new IOException("Неверный формат файла игрока " + path
                    + ": нужно 4 строки (фамилия, имя, группа, путь к карте)");
        }

        String lastName = lines.get(0).trim();
        String firstName = lines.get(1).trim();
        String group = lines.get(2).trim();
        String cardPath = lines.get(3).trim();
        if (!cardPath.endsWith(".txt")) {
            cardPath += ".txt";
        }

        CreatureCard card = cardLoad(cardPath);
        return new Student(lastName + "_" + firstName, lastName, firstName, group, card);
    }

    @Override
    public CreatureCard cardLoad(String pathToCardFile) throws IOException {
        List<String> lines = readNonEmptyLines(pathToCardFile);
        if (lines.size() < 7) {
            throw new IOException("Неверный формат файла карты " + pathToCardFile
                    + ": нужно 7 строк, найдено " + lines.size());
        }

        int last = lines.size() - 1;
        String name = lines.get(0).trim();
        ManaCost manaCost = parseManaCost(lines.get(1));
        CardType cardType = parseCardType(lines.get(2));
        String abilitiesLine = lines.get(3).trim();
        String oracleText = String.join("\n", lines.subList(4, last - 1)).trim();
        String powerToughness = lines.get(last - 1).trim();
        String setTitle = lines.get(last).trim();

        if (oracleText.equals("-")) {
            oracleText = "";
        }

        int slash = powerToughness.indexOf('/');
        if (slash < 0) {
            throw new IOException("Неверная строка силы/выносливости: " + powerToughness);
        }
        int power;
        int toughness;
        try {
            power = Integer.parseInt(powerToughness.substring(0, slash).trim());
            toughness = Integer.parseInt(powerToughness.substring(slash + 1).trim());
        } catch (NumberFormatException e) {
            throw new IOException("Неверная строка силы/выносливости: " + powerToughness, e);
        }

        CreatureCard card = new CreatureCard(name, manaCost, cardType, setTitle,
                oracleText, power, toughness, new HashMap<>());

        for (Ability ability : parseAbilities(abilitiesLine)) {
            card.addAbility(ability);
        }
        return card;
    }

    @Override
    public CreatureCard cardImport(String cardFileName) throws IOException {
        File file = new File(cardFileName);
        if (!file.exists() && !cardFileName.endsWith(".crd")) {
            file = new File(cardFileName + ".crd");
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object loaded = in.readObject();
            if (!(loaded instanceof CreatureCard card)) {
                throw new IOException("Файл " + file + " не содержит карту существа");
            }
            return card;
        } catch (ClassNotFoundException e) {
            throw new IOException("Файл " + file + " не содержит карту существа", e);
        }
    }

    @Override
    public void cardExport(CreatureCard creatureCard) throws IOException {
        File file = new File(creatureCard.getName() + ".crd");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(creatureCard);
        }
    }

    // ---------- разбор строк карты ----------

    private ManaCost parseManaCost(String line) {
        int generic = 0;
        Map<ManaType, Integer> colored = new EnumMap<>(ManaType.class);

        Matcher matcher = MANA_SYMBOL.matcher(line);
        while (matcher.find()) {
            String symbol = matcher.group(1).trim();
            if (symbol.matches("\\d+")) {
                generic += Integer.parseInt(symbol);
                continue;
            }
            for (ManaType type : ManaType.values()) {
                if (type.getSymbol().equalsIgnoreCase(symbol)) {
                    colored.merge(type, 1, Integer::sum);
                }
            }
        }

        ManaCost manaCost = new ManaCost(generic);
        // addColoredMana заменяет значение, поэтому передаём уже посчитанные количества
        colored.forEach(manaCost::addColoredMana);
        return manaCost;
    }

    private CardType parseCardType(String line) {
        String left = line;
        String subType = "";

        int dash = line.indexOf('—');
        if (dash < 0) {
            dash = line.indexOf(" - ");
        }
        if (dash >= 0) {
            left = line.substring(0, dash);
            subType = line.substring(dash + 1).replaceFirst("^\\s*-?\\s*", "").trim();
        }

        SuperType superType = SuperType.NONE;
        List<String> typeWords = new ArrayList<>();
        for (String word : left.trim().split("\\s+")) {
            SuperType found = findSuperType(word);
            if (superType == SuperType.NONE && found != null) {
                superType = found;
            } else {
                typeWords.add(word);
            }
        }
        return new CardType(superType, String.join(" ", typeWords), subType);
    }

    private SuperType findSuperType(String word) {
        for (SuperType type : SuperType.values()) {
            if (type != SuperType.NONE && type.name().equalsIgnoreCase(word)) {
                return type;
            }
        }
        return null;
    }

    private List<Ability> parseAbilities(String line) {
        List<Ability> result = new ArrayList<>();
        if (line.isBlank() || line.equals("-")) {
            return result;
        }
        for (String token : line.split(",")) {
            String name = token.trim();
            String key = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            Function<String, Ability> factory = ABILITIES.get(key);
            if (factory != null) {
                result.add(factory.apply(name));
            }
        }
        return result;
    }

    // ---------- чтение файлов ----------

    /** Читает файл с диска, а если его там нет - из classpath (например, из src/main/resources). */
    private List<String> readNonEmptyLines(String path) throws IOException {
        byte[] bytes;
        File file = new File(path);
        if (file.isFile()) {
            bytes = java.nio.file.Files.readAllBytes(file.toPath());
        } else {
            bytes = readFromClasspath(path);
        }

        List<String> lines = new ArrayList<>(List.of(
                new String(bytes, StandardCharsets.UTF_8).replace("\r", "").split("\n", -1)));
        while (!lines.isEmpty() && lines.getLast().isBlank()) {
            lines.removeLast();
        }
        return lines;
    }

    private byte[] readFromClasspath(String path) throws IOException {
        String normalized = path.replace('\\', '/');
        for (String prefix : new String[]{MAIN_RESOURCES, TEST_RESOURCES}) {
            if (normalized.startsWith(prefix)) {
                normalized = normalized.substring(prefix.length());
            }
        }
        try (InputStream in = FileGameIO.class.getClassLoader().getResourceAsStream(normalized)) {
            if (in == null) {
                throw new FileNotFoundException("Файл не найден: " + path);
            }
            return in.readAllBytes();
        }
    }
}
