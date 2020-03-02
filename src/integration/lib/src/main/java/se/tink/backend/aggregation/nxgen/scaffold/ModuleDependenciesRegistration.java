package se.tink.backend.aggregation.nxgen.scaffold;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public abstract class ModuleDependenciesRegistration
        implements AgentDependenciesRegistrationLifecycle {

    private final ModuleDependenciesRegistry moduleDependenciesRegistry =
            new SimpleModuleDependenciesRegistry();

    @Override
    public ModuleDependenciesRegistry createModuleDependenciesRegistry() {
        return moduleDependenciesRegistry;
    }

    @Override
    public void registerExternalDependencies(
            TinkHttpClient tinkHttpClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        registerBean(TinkHttpClient.class, tinkHttpClient);
        registerBean(SessionStorage.class, sessionStorage);
        registerBean(PersistentStorage.class, persistentStorage);
    }

    protected <T> void registerBean(Class<T> clazz, Object bean) {
        moduleDependenciesRegistry.registerBean(clazz, bean);
    }

    protected void registerBean(Object bean) {
        moduleDependenciesRegistry.registerBean(bean.getClass(), bean);
    }

    protected <T> T getBean(Class<T> clazz) {
        return moduleDependenciesRegistry.getBean(clazz);
    }
}
