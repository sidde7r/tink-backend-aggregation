package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class KeySignerProviderTest {

    public static final String EIDAS_CERT_ID = "cert_id";
    public static final String APP_ID = "app_id";
    @Mock private UkOpenBankingConfiguration configuration;
    @Mock private AgentsServiceConfiguration agentsServiceConfiguration;

    @Mock private CompositeAgentContext context;
    @Mock private RandomValueGenerator randomValueGenerator;
    @Mock private EidasProxyConfiguration eidasProxyConfiguration;
    @Mock private AgentConfiguration<UkOpenBankingConfiguration> ukConfiguration;
    @Mock private CredentialsRequest credentialsRequest;
    @Mock private Provider provider;
    private KeySignerProvider keySignerProvider;

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
        when(agentComponentProvider.getRandomValueGenerator()).thenReturn(randomValueGenerator);

        AgentConfigurationControllerable agentConfigurationControllerable =
                mock(AgentConfigurationControllerable.class);
        when(agentComponentProvider.getContext().getAgentConfigurationController())
                .thenReturn(agentConfigurationControllerable);

        when(agentConfigurationControllerable.getAgentConfiguration(
                        UkOpenBankingConfiguration.class))
                .thenReturn(ukConfiguration);
        when(ukConfiguration.getQsealc()).thenReturn(EIdasTinkCert.QSEALC);
        when(agentComponentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);
        when(credentialsRequest.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("uk-aib-oauth2");
        // agentComponentProvider.getCredentialsRequest().getProvider().getName()))

        keySignerProvider =
                new KeySignerProvider(
                        configuration,
                        agentComponentProvider,
                        agentsServiceConfiguration,
                        EIDAS_CERT_ID);
    }

    @Test
    public void shouldAllowDirectionForEidasProxyJwtSignerWhenClusterIdAndTestMatching() {
        // given
        when(randomValueGenerator.generateRandomDoubleInRange(0, 100)).thenReturn(4d);

        // when
        JwtSigner jwtSigner = keySignerProvider.get();

        // then
        assertThat(jwtSigner).isInstanceOf(PayloadEncodedSecretServiceJwtSigner.class);
    }

    @Test
    public void shouldUseLegacySolutionWhenTestIsNotMatching() {
        // given
        when(randomValueGenerator.generateRandomDoubleInRange(0, 100)).thenReturn(50d);

        // when
        JwtSigner jwtSigner = keySignerProvider.get();

        // then
        assertThat(jwtSigner).isInstanceOf(PayloadEncodedSecretServiceJwtSigner.class);
    }
}
