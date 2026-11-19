package ru.mirea.hidden.tests.ioc.configurators;

public interface ProxyConfigurator {
    Object replaceWithProxyIfNeeded(Object instance, Class<?> implClass);
}
