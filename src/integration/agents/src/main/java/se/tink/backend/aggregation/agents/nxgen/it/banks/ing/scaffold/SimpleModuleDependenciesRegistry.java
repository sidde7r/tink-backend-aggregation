package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import java.util.HashMap;
import java.util.Map;

public class SimpleModuleDependenciesRegistry implements ModuleDependenciesRegistry {

    private final Map<String, Object> registry = new HashMap<>();

    @Override
    public <T> T getBean(Class<T> clazz) {
        if (registry.containsKey(beanKey(clazz))) {
            return (T) registry.get(beanKey(clazz));
        }
        throw new BeanNotFoundException(
                String.format("Bean '%s' is not registered", beanKey(clazz)));
    }

    @Override
    public <T> void registerBean(Class<T> clazz, Object bean) {
        registry.put(beanKey(clazz), bean);
    }

    private <T> String beanKey(Class<T> clazz) {
        return clazz.getSimpleName();
    }
}
