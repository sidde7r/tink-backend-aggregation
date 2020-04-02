package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseMessageSignInterceptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class CreditAgricoleBaseClientConfigurationUtilsTest {

    @Test
    public void shouldGetConfiguration() {
        // given
        AgentsServiceConfiguration configuration = mock(AgentsServiceConfiguration.class);
        CreditAgricoleBaseApiClient apiClient = mock(CreditAgricoleBaseApiClient.class);
        TinkHttpClient client = mock(TinkHttpClient.class);
        AgentContext context = mock(AgentContext.class);
        Class agentClass = Class.class;
        AgentConfigurationControllerable agentConfigurationController =
                mock(AgentConfigurationControllerable.class);
        Class<CreditAgricoleBaseConfiguration> clientConfigurationClass =
                CreditAgricoleBaseConfiguration.class;
        CreditAgricoleBaseConfiguration creditAgricoleBaseConfiguration =
                mock(CreditAgricoleBaseConfiguration.class);
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        String clusterId = "clusterId";
        String appId = "appId";

        when(agentConfigurationController.getAgentConfiguration(clientConfigurationClass))
                .thenReturn(creditAgricoleBaseConfiguration);
        when(configuration.getEidasProxy()).thenReturn(eidasProxyConfiguration);
        when(context.getClusterId()).thenReturn(clusterId);
        when(context.getAppId()).thenReturn(appId);

        doNothing().when(apiClient).setConfiguration(creditAgricoleBaseConfiguration);
        doNothing()
                .when(client)
                .setMessageSignInterceptor(any(CreditAgricoleBaseMessageSignInterceptor.class));

        // when
        CreditAgricoleBaseConfiguration resp =
                CreditAgricoleBaseClientConfigurationUtils.getConfiguration(
                        configuration,
                        apiClient,
                        client,
                        context,
                        agentClass,
                        agentConfigurationController,
                        clientConfigurationClass);

        // then
        assertThat(resp, instanceOf(CreditAgricoleBaseConfiguration.class));
        verify(apiClient, times(1)).setConfiguration(any());
        verify(client, times(1)).setEidasProxy(any());
    }
}
