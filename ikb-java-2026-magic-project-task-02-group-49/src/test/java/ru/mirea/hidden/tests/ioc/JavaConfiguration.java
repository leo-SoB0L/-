package ru.mirea.hidden.tests.ioc;

import org.reflections.Reflections;

import java.util.Map;
import java.util.Set;

public class JavaConfiguration implements EngineConfiguration {

    private final Reflections scanner;

    private final Map<Class, Class> ifc2ImplClass;

    public JavaConfiguration(String packageToScan, Map<Class, Class> ifc2ImplClass) {
        this.ifc2ImplClass = ifc2ImplClass;
        this.scanner = new Reflections(packageToScan);
    }

    @Override
    public Reflections getScanner() {
        return this.scanner;
    }

    @Override
    public <T> Class<? extends T> getComponentImpl(Class<T> ifc) {
        if (!ifc2ImplClass.containsKey(ifc)) {
            System.out.printf("🔎Ищем все реализации интерфейса %s%n", ifc.getSimpleName());

            Set<Class<? extends T>> classes = scanner.getSubTypesOf(ifc);

            //Set<Class<? extends T>> classesWithComponentAnnotate = classes.stream().filter(cls -> cls.isAnnotationPresent(Component.class)).collect(Collectors.toSet());

            Class<?> singleImplement = null;

            if (classes.size() > 1) {
                throw new RuntimeException("❌ More than one impl for this component");
            }

            if (classes.isEmpty()) {
                return null;
            }

            singleImplement = classes.iterator().next();

            System.out.printf("✅Нашли нужную и единственную реализацию %s%n", singleImplement.getSimpleName());

            ifc2ImplClass.put(ifc, singleImplement);

            return (Class<? extends T>) singleImplement;
        }
        return ifc2ImplClass.get(ifc);
    }
}
