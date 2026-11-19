package ru.mirea.hidden.tests.ioc;

import ru.mirea.hidden.tests.ioc.configurators.ComponentConfigurator;
import ru.mirea.hidden.tests.ioc.configurators.ProxyConfigurator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentFactory {
    private final List<ComponentConfigurator> configurators;
    private final List<ProxyConfigurator> proxyConfigurators;
    private final ComponentContext componentContext;

    public ComponentFactory(ComponentContext componentContext) {
        try {
            this.componentContext = componentContext;
            configurators = new ArrayList<>();
            proxyConfigurators = new ArrayList<>();
            for (Class<? extends ComponentConfigurator> aClass : componentContext
                    .getEngineConfiguration()
                    .getScanner()
                    .getSubTypesOf(ComponentConfigurator.class)) {
                configurators.add(aClass.getDeclaredConstructor().newInstance());
            }

            for (Class<? extends ProxyConfigurator> aClass : componentContext
                    .getEngineConfiguration()
                    .getScanner()
                    .getSubTypesOf(ProxyConfigurator.class)) {
                proxyConfigurators.add(aClass.getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T createComponent(Class<T> implClass) {
        T instance = create(implClass);

        for (ComponentConfigurator configurator : configurators) {
            configurator.configure(instance, componentContext);
        }

        for (ProxyConfigurator proxyConfigurator : proxyConfigurators) {
            instance = (T) proxyConfigurator.replaceWithProxyIfNeeded(instance, implClass);
        }

        return instance;
    }

    private <T> T create(Class<T> implClass) {
        List<Constructor<?>> list = Arrays.stream(implClass.getConstructors())
                .filter(constructor -> constructor.getParameterCount() >= 1
                        || constructor.getParameterCount() == 0).toList();

        if (list.size() > 1) {
            throw new RuntimeException("❌Component constructor must be ether not declared or single");
        }

        Constructor<?> constructor = list.getFirst();

        System.out.printf("🔄Конструктор реализации %s%n", constructor.toString());

        if (constructor.getParameterCount() > 0) {

            System.out.println("🔎Ищем параметры конструктора");

            List<Class<?>> parameters = new ArrayList<>();

            List<Object> objects = new ArrayList<>();

            for (Parameter parameter : constructor.getParameters()) {
                System.out.printf("✅Найден параметр %s%n", parameter.toString());
                parameters.add(parameter.getType());
            }

            for (Class<?> parameter : parameters) {
                System.out.printf("🔄Инициализируем параметр %s%n", parameter.getSimpleName());
                Object component = componentContext.getComponent(parameter);
                objects.add(component);
            }

            System.out.printf("🔄Инициализируем реализацию %s параметрами: %s %n", implClass.getSimpleName(), objects);
            try {
                return implClass.getDeclaredConstructor(parameters.toArray(new Class<?>[0])).newInstance(objects.toArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.printf("🔄Инициализация реализации %s конструктором без параметров %n", implClass.getSimpleName());
        try {
            return implClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
