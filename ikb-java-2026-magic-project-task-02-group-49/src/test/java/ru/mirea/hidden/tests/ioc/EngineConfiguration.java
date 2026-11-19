package ru.mirea.hidden.tests.ioc;

import org.reflections.Reflections;

public interface EngineConfiguration {

    Reflections getScanner();

    <T> Class<? extends T> getComponentImpl(Class<T> ifc);
}
