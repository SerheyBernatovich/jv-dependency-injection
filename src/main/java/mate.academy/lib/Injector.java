package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }
        Object instance = instances.get(clazz);
        if (instance == null) {
            instance = createInstance(clazz);
            instances.put(clazz, instance);
            injectFields(clazz, instance);
        }
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.equals(FileReaderService.class)) {
            return FileReaderServiceImpl.class;
        }
        if (interfaceClazz.equals(ProductParser.class)) {
            return ProductParserImpl.class;
        }
        if (interfaceClazz.equals(ProductService.class)) {
            return ProductServiceImpl.class;
        }
        return interfaceClazz;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
        }
    }

    private void injectFields(Class<?> clazz, Object instance) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field " + field.getName(), e);
                }
            }
        }
    }
}
