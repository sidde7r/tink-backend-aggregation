package se.tink.backend.aggregation.nxgen.scaffold;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public interface AgentDependenciesRegistrationLifecycle {

    void registerExternalDependencies(
            TinkHttpClient tinkHttpClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage);

    void registerInternalModuleDependencies();

    ModuleDependenciesRegistry createModuleDependenciesRegistry();
}
