package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public interface AgentDependenciesRegistrationLifecycle {

    void registerExternalDependencies(TinkHttpClient tinkHttpClient, SessionStorage sessionStorage);

    void registerInternalModuleDependencies();

    ModuleDependenciesRegistry createModuleDependenciesRegistry();
}
