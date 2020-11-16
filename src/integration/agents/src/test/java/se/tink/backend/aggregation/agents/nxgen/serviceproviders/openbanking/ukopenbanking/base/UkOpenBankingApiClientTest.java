package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingTestFixtures.JWKS_EXAMPLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingTestFixtures.WELL_KNOWN_EXAMPLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingTestFixtures.WELL_KNOWN_URL;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class UkOpenBankingApiClientTest {

    private TinkHttpClient httpClient;
    private UkOpenBankingApiClient apiClient;

    @Before
    public void setup() {
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        final SoftwareStatementAssertion softwareStatementAssertionMock =
                mock(SoftwareStatementAssertion.class);
        final String redirectUrl = "http://redirect-url";
        final ClientInfo clientInfoMock = mock(ClientInfo.class);
        final RandomValueGenerator randomValueGeneratorMock = mock(RandomValueGenerator.class);
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);
        final UkOpenBankingAisConfig ukOpenBankingAisConfigMock =
                createUkOpenBankingAisConfigMock();
        final UkOpenBankingPisConfig ukOpenBankingPisConfigMock =
                mock(UkOpenBankingPisConfig.class);

        httpClient = mock(TinkHttpClient.class);
        apiClient =
                new UkOpenBankingApiClient(
                        httpClient,
                        jwtSignerMock,
                        softwareStatementAssertionMock,
                        redirectUrl,
                        clientInfoMock,
                        randomValueGeneratorMock,
                        persistentStorageMock,
                        ukOpenBankingAisConfigMock,
                        ukOpenBankingPisConfigMock);
    }

    @Test
    public void shouldGetJwksPublicKeys() {
        RequestBuilder wellKnownRequestBuilder = mockRequestBuilder(WELL_KNOWN_EXAMPLE);
        when(httpClient.request(eq(WELL_KNOWN_URL))).thenReturn(wellKnownRequestBuilder);
        RequestBuilder jwksRequestBuilder = mockRequestBuilder(JWKS_EXAMPLE);
        when(httpClient.request(eq(new URL("http://jwks")))).thenReturn(jwksRequestBuilder);

        Optional<Map<String, PublicKey>> jwkPublicKeys = apiClient.getJwkPublicKeys();
        assertThat(jwkPublicKeys).isPresent();
        assertThat(jwkPublicKeys.get()).hasSize(1);
        assertThat(jwkPublicKeys.get().get("external").getAlgorithm()).isEqualTo("RSA");
        assertThat(jwkPublicKeys.get().get("external").getFormat()).isEqualTo("X.509");
    }

    private RequestBuilder mockRequestBuilder(String contents) {
        RequestBuilder toReturn = mock(RequestBuilder.class);
        when(toReturn.get(eq(String.class))).thenReturn(contents);
        return toReturn;
    }

    private static UkOpenBankingAisConfig createUkOpenBankingAisConfigMock() {
        final UkOpenBankingAisConfig aisConfigMock = mock(UkOpenBankingAisConfig.class);

        when(aisConfigMock.getWellKnownURL()).thenReturn(WELL_KNOWN_URL);

        return aisConfigMock;
    }
}
