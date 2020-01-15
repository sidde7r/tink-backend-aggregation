package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
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
            TinkHttpClient tinkHttpClient, SessionStorage sessionStorage) {
        registerBean(TinkHttpClient.class, tinkHttpClient);
        registerBean(SessionStorage.class, sessionStorage);
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
