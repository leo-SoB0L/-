package ru.mirea.hidden.tests.ioc;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentContext {
    private ComponentFactory componentFactory;
    private final Map<Class, Object> cache = new ConcurrentHashMap<>();
    private final EngineConfiguration engineConfiguration;

    public ComponentContext(EngineConfiguration configurator) {
        this.engineConfiguration = configurator;
    }

    public <T> T getComponent(Class<T> type) {
        if (cache.containsKey(type)) {
            System.out.printf("✅Компонент %s найден в кэше, возвращаем%n", type.getSimpleName());
            return (T) cache.get(type);
        }

        Class<? extends T> implClass = type;

        if (type.isInterface()) {
            System.out.printf("🔄Ищем класс-реализацию компонента %s%n", type.getSimpleName());
            implClass = engineConfiguration.getComponentImpl(type);
        }

        if(Objects.isNull(implClass)){
           throw new RuntimeException("❌No implement class found for this interface %s".formatted(type));
        }

        System.out.printf("🔄Создаём компонент %s%n", implClass.getSimpleName());

        return componentFactory.createComponent(implClass);
    }

    public void setComponentFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public EngineConfiguration getEngineConfiguration() {
        return engineConfiguration;
    }

    public <V, K> Map<K,V> getCache() {
        return (Map<K, V>) cache;
    }
}
