package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyWithFallbackJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.EidasJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class DanskeJwtSignerProviderTest {

    private static final String APP_ID = "app_id";

    @Mock private DanskebankEUConfiguration configuration;
    @Mock private AgentsServiceConfiguration agentsServiceConfiguration;

    @Mock private CompositeAgentContext context;
    @Mock private EidasProxyConfiguration eidasProxyConfiguration;
    @Mock private AgentConfiguration<DanskebankEUConfiguration> agentConfiguration;

    private DanskeJwtSignerProvider danskeJwtSignerProvider;

    @Before
    public void setUp() throws Exception {
        AgentComponentProvider agentComponentProvider =
                new AgentComponentProvider(
                        mock(TinkHttpClientProvider.class),
                        mock(SupplementalInformationProvider.class),
                        mock(AgentContextProvider.class),
                        mock(GeneratedValueProvider.class));
        when(agentComponentProvider.getContext()).thenReturn(context);
        when(context.getAppId()).thenReturn(APP_ID);
        when(agentsServiceConfiguration.getEidasProxy()).thenReturn(eidasProxyConfiguration);
        when(eidasProxyConfiguration.toInternalConfig())
                .thenReturn(mock(InternalEidasProxyConfiguration.class));

        AgentConfigurationControllerable agentConfigurationControllerable =
                mock(AgentConfigurationControllerable.class);
        when(agentComponentProvider.getContext().getAgentConfigurationController())
                .thenReturn(agentConfigurationControllerable);

        when(agentConfigurationControllerable.getAgentConfiguration(
                        DanskebankEUConfiguration.class))
                .thenReturn(agentConfiguration);

        danskeJwtSignerProvider =
                new DanskeJwtSignerProvider(
                        agentComponentProvider, agentsServiceConfiguration, configuration);
    }

    @Test
    public void shouldUseEidasProxyWithFallbackJwtSigner() {
        // given
        when(agentConfiguration.getQsealc()).thenReturn(EIdasTinkCert.QSEALC);
        SoftwareStatementAssertion ssa = mock(SoftwareStatementAssertion.class);
        when(configuration.getSoftwareStatementAssertions()).thenReturn(ssa);
        when(ssa.getJwksEndpoint()).thenReturn(new URL("https://test.com"));

        // when
        JwtSigner jwtSigner = danskeJwtSignerProvider.get();

        // then
        assertThat(jwtSigner).isInstanceOf(EidasProxyWithFallbackJwtSigner.class);
    }

    @Test
    public void shouldUseLegacySolutionWhenThereIsAnIssueWithQSealc() {
        // given
        when(agentConfiguration.getQsealc()).thenReturn(null);

        // when
        JwtSigner jwtSigner = danskeJwtSignerProvider.get();

        // then
        assertThat(jwtSigner).isInstanceOf(EidasJwtSigner.class);
    }
}
