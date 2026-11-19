package ru.mirea.hidden.tests.ioc.configurators;

import ru.mirea.hidden.tests.ioc.ComponentContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldsComponentConfigurator implements ComponentConfigurator {

    @Override
    public void configure(Object component, ComponentContext componentContext) {
        try {

            System.out.println("[INIT]FieldsComponentConfigurator");
            System.out.printf("[INIT-CONFIG]Конфигурируем поля компонента %s%n", component.getClass().getSimpleName());
            for (Field field : component.getClass().getDeclaredFields()) {
                System.out.printf("[INIT-CONFIG]Конфигурируем поле %s%n", field.getName());

                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                Object o = field.get(component);

                if (o != null) {
                    System.out.printf("[INIT-CONFIG]Поле уже инициализировано (%s)%n", o);
                    continue;
                }
                // Не хотим конфигурировать поле, если это не интерфейс, это примитив, он final, это закрытый для изменения пакет
                if (!fieldType.isInterface()
                        || fieldType.isPrimitive()
                        || Modifier.isFinal(field.getModifiers())
                        || fieldType.getPackage().getName().startsWith("java.")) {
                    System.out.println("[INIT-CONFIG]Не конфигурируем поле");
                    continue;
                }

                Object fieldObject = componentContext.getComponent(fieldType);

                System.out.printf("[INIT-CONFIG]Инициализируем поле %s объектом %s%n", field.getName(), fieldObject.toString());

                field.set(component, fieldObject);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
