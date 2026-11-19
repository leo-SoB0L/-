package ru.mirea.hidden.tests.ioc;

public class ApplicationContext {
    private static ComponentContext componentContext;

    public static ComponentContext getComponentContext() {
        return componentContext;
    }

    public static void setComponentContext(ComponentContext context) {
        componentContext = context;
    }

}
