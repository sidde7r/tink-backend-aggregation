package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.nxgen.http.NextGenRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcApiClientTest {

    private static final String BASE_URL = "https://base-url";
    private static final String REDIRECT_URL = "https://redirect-url";
    private static final String TEST_URL = BASE_URL + "/psd2/v2";
    private static final String PSU_IP_ADDR = "0.0.0.0";
    private static final String CONSENT_ID = "1234";
    private static final String QSEALC = "QSEALC";

    private PersistentStorage persistentStorage;
    private KbcApiClient kbcApiClient;

    @Before
    public void setUp() {
        final TinkHttpClient httpClientMock = mock(TinkHttpClient.class);
        persistentStorage = new PersistentStorage();
        final KbcConfiguration kbcConfigurationMock = mock(KbcConfiguration.class);
        final CredentialsRequest requestMock = mock(CredentialsRequest.class);

        when(kbcConfigurationMock.getBaseUrl()).thenReturn(BASE_URL);
        when(kbcConfigurationMock.getPsuIpAddress()).thenReturn(PSU_IP_ADDR);

        setUpHttpClientMock(httpClientMock);

        kbcApiClient =
                new KbcApiClient(
                        httpClientMock,
                        kbcConfigurationMock,
                        requestMock,
                        REDIRECT_URL,
                        persistentStorage,
                        QSEALC);
    }

    @Test
    public void shouldBuildTransactionsRequestFromStorage() {
        // given
        Token accessToken =
                Token.builder().body("TOKEN").tokenType("Bearer").expiresIn(0L, 0L).build();
        Token refreshToken = Token.builder().body("TOKEN").tokenType("refreshToken").build();
        RefreshableAccessToken refreshableAccessToken =
                RefreshableAccessToken.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
        KbcAuthenticationData kbcAuthenticationData = new KbcAuthenticationData();
        kbcAuthenticationData.setConsentId(CONSENT_ID);
        persistentStorage.put(
                "RedirectTokens", SerializationUtils.serializeToString(refreshableAccessToken));
        persistentStorage.put(
                "KBC_AUTHENTICATION_DATA",
                SerializationUtils.serializeToString(kbcAuthenticationData));
        // when
        RequestBuilder requestBuilder = kbcApiClient.getTransactionsRequestBuilder(TEST_URL);
        // then
        MultivaluedMap<String, Object> headers = requestBuilder.build(HttpMethod.GET).getHeaders();
        String authorizationHeader = (String) headers.getFirst(HttpHeaders.AUTHORIZATION);
        String consentId = (String) headers.getFirst(BerlinGroupConstants.HeaderKeys.CONSENT_ID);
        Assert.assertEquals("Bearer TOKEN", authorizationHeader);
        Assert.assertEquals(CONSENT_ID, consentId);
    }

    private static void setUpHttpClientMock(TinkHttpClient httpClientMock) {
        final NextGenRequestBuilder requestBuilder = new NextGenRequestBuilder(null, "test", null);
        when(httpClientMock.request(TEST_URL)).thenReturn(requestBuilder);
    }
}
