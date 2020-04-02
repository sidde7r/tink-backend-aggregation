package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import se.tink.backend.aggregation.agents.contexts.EidasContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseMessageSignInterceptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class CreditAgricoleBaseClientConfigurationUtils {

    public static CreditAgricoleBaseConfiguration getConfiguration(
            final AgentsServiceConfiguration configuration,
            final CreditAgricoleBaseApiClient apiClient,
            final TinkHttpClient client,
            final EidasContext context,
            final Class agentClass,
            final AgentConfigurationControllerable agentConfigurationController,
            final Class<CreditAgricoleBaseConfiguration> clientConfigurationClass) {
        final CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                agentConfigurationController.getAgentConfiguration(clientConfigurationClass);

        apiClient.setConfiguration(creditAgricoleConfiguration);
        client.setMessageSignInterceptor(
                new CreditAgricoleBaseMessageSignInterceptor(
                        creditAgricoleConfiguration,
                        configuration.getEidasProxy(),
                        new EidasIdentity(context.getClusterId(), context.getAppId(), agentClass)));

        client.setEidasProxy(configuration.getEidasProxy());

        return creditAgricoleConfiguration;
    }
}
