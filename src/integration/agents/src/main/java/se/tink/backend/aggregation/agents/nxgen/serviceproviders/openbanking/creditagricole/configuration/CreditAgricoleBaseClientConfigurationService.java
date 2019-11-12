package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseMessageSignInterceptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class CreditAgricoleBaseClientConfigurationService {

    private static CreditAgricoleBaseClientConfigurationService instance;

    private CreditAgricoleBaseClientConfigurationService() {}

    public static CreditAgricoleBaseClientConfigurationService getInstance() {
        if (instance == null) {
            instance = new CreditAgricoleBaseClientConfigurationService();
        }
        return instance;
    }

    public CreditAgricoleBaseConfiguration getConfiguration(
            final AgentsServiceConfiguration configuration,
            final String clientName,
            final CreditAgricoleBaseApiClient apiClient,
            final TinkHttpClient client,
            final AgentContext context,
            final Class agentClass,
            final AgentConfigurationController agentConfigurationController) {
        final CreditAgricoleBaseConfiguration creditAgricoleConfiguration =
                agentConfigurationController.getAgentConfigurationFromK8s(
                        CreditAgricoleBaseConstants.INTEGRATION_NAME,
                        clientName,
                        CreditAgricoleBaseConfiguration.class);

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
