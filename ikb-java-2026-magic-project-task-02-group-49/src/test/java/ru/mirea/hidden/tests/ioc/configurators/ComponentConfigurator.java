package ru.mirea.hidden.tests.ioc.configurators;


import ru.mirea.hidden.tests.ioc.ComponentContext;

public interface ComponentConfigurator {
    void configure(Object component, ComponentContext componentContext);
}
