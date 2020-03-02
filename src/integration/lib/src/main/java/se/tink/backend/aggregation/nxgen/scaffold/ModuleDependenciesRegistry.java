package se.tink.backend.aggregation.nxgen.scaffold;

public interface ModuleDependenciesRegistry {

    class BeanNotFoundException extends RuntimeException {

        BeanNotFoundException(String message) {
            super(message);
        }
    }

    <T> void registerBean(Class<T> clazz, Object bean);

    <T> T getBean(Class<T> clazz);
}
