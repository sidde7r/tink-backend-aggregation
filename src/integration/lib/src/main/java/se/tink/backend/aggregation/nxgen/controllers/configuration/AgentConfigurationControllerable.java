package se.tink.backend.aggregation.nxgen.controllers.configuration;

import io.reactivex.rxjava3.core.Observable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface AgentConfigurationControllerable {

    <T extends ClientConfiguration> T getAgentConfiguration(final Class<T> clientConfigClass);

    void completeSecretValuesSubject();

    Observable<Collection<String>> getSecretValuesObservable();

    boolean isOpenBankingAgent();

    <T extends ClientConfiguration> T getAgentConfigurationFromK8s(
            String integrationName, String clientName, Class<T> clientConfigClass);

    <T extends ClientConfiguration> T getAgentConfigurationFromK8s(
            String integrationName, Class<T> clientConfigClass);

    Set<String> extractSensitiveValues(Object clientConfigurationAsObject);

    <T extends ClientConfiguration> Optional<T> getAgentConfigurationFromK8sAsOptional(
            String integrationName, Class<T> clientConfigClass);
}
