package ru.mirea.magic.gamelogic;

import ru.mirea.magic.card.CreatureCard;
import ru.mirea.magic.card.Student;

import java.io.IOException;
import java.util.ResourceBundle;

public interface GameIO {

    /**
     * Метод для загрузки ресурсных файлов (из папки Resources)
     * Для загрузки нужно использовать метод класса {@code PropertyResourceBundle.getBundle("Путь\к\ресурсу");}
     * Поиск ресурса осуществляется внутри папки src/main/resources по расширению .properties, таким образом
     * Для поиска ресурса example.properties достаточно передать название example.
     * Пример поиска ресурса внутри подкаталога resources/game-resources:
     * {@snippet lang=java :
     * public ResourceBundle resourceLoad(String resourceName){
     *     return PropertyResourceBundle.getBundle("game-resources/"+resourceName);
     * }
     * }
     * ResourceBundle это словарь, где ключ это название свойство, а значение - данные написанные после знака '='
     * @param resourceName имя ресурса <u>без указания</u> расширения properties
     * @return ресурсный пакет
     */
    public ResourceBundle resourceLoad(String resourceName);

    /**
     * Метод загрузки игрока из файла с именем 'имя_игрока.txt'. При загрузке игрока загружается его карта.
     * @param playerName имя игрока
     * @return объект Student
     */
    Student playerLoad(String playerName) throws IOException;

    /**
     * Метод для загрузки карты из файла 'имя_карты.txt', согласно формату описания карты.
     * Последовательно создаёт объект карты из составных частей.
     * @param pathToCardFile путь к файлу карты
     * @return объект Card
     */
    CreatureCard cardLoad(String pathToCardFile) throws IOException;

    /**
     * Метод для импорта объекта карты из сериализованного файла с расширением .crd
     * @param cardFileName путь к файлу
     * @return загруженный объект карты
     * @throws IOException если что-то пойдет не так во время импорта
     */
    CreatureCard cardImport(String cardFileName) throws IOException;

    /**
     * Метод для экспорта объекта карты в сериализованный файл с расширением .crd
     * @param creatureCard объект карты для сериализации
     * @throws IOException если что-то пойдет не так во время экспорта
     */
    void cardExport(CreatureCard creatureCard) throws IOException;
}
