package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.core.MediaType;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class UkOpenBankingApiClientTest {

    private final String accountId = "dummyAccountId";
    private final URL requestUrl = URL.of("dummyUrl");
    private UkOpenBankingApiClient ukOpenBankingApiClient;
    private HttpResponse httpResponse;

    @BeforeEach
    void setUp() {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        UkOpenBankingAisConfig aisConfig = mock(UkOpenBankingAisConfig.class);
        when(aisConfig.getWellKnownURL()).thenReturn(URL.of("https://initDummyURL.com"));
        when(tinkHttpClient.addFilter(any())).thenReturn(null);
        ukOpenBankingApiClient =
                new UkOpenBankingApiClient(
                        tinkHttpClient,
                        mock(JwtSigner.class),
                        mock(SoftwareStatementAssertion.class),
                        "https://dummyRedirectUrl.com",
                        mock(ClientInfo.class),
                        mock(RandomValueGenerator.class),
                        mock(PersistentStorage.class),
                        aisConfig,
                        mock(AgentComponentProvider.class));

        // given
        when(aisConfig.getAccountBalanceRequestURL(anyString())).thenReturn(requestUrl);

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(requestUrl)).thenReturn(requestBuilder);
        when(requestBuilder.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(requestBuilder);
        when(requestBuilder.addFilter(any())).thenReturn(requestBuilder);

        HttpResponseException responseException = mock(HttpResponseException.class);
        when(requestBuilder.get(AccountBalanceV31Response.class)).thenThrow(responseException);

        httpResponse = mock(HttpResponse.class);
        when(responseException.getResponse()).thenReturn(httpResponse);
    }

    @Test
    void shouldThrowSessionExpiredExceptionWhileFetchingAccountBalancesAndWhenReceived400Status()
            throws JsonProcessingException {
        // given
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(getSampleErrorResponse());

        // when
        ThrowingCallable throwingCallable =
                () -> ukOpenBankingApiClient.fetchV31AccountBalances(accountId);
        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> assertThat(e.getError()).isEqualTo(SessionError.SESSION_EXPIRED));
    }

    @Test
    void shouldThrowAccountRefreshExceptionWhenReceiveStatusIsNeither400Nor403() {
        // given
        when(httpResponse.getStatus()).thenReturn(404);

        // when
        ThrowingCallable throwingCallable =
                () -> ukOpenBankingApiClient.fetchV31AccountBalances(accountId);
        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(AccountRefreshException.class);
    }

    @Test
    void shouldThrowAccountRefreshExceptionWhenReceiveStatusIs400AndHaveEmptyErrorList()
            throws JsonProcessingException {
        // given
        when(httpResponse.getStatus()).thenReturn(404);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(getSampleErrorResponseWithNoErrors());

        // when
        ThrowingCallable throwingCallable =
                () -> ukOpenBankingApiClient.fetchV31AccountBalances(accountId);
        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(AccountRefreshException.class);
    }

    private ErrorResponse getSampleErrorResponse() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse =
                "{\n"
                        + "  \"Code\" : \"400\",\n"
                        + "  \"Id\" : \"cd22e7fb-4f0b-4f24-b2b3-f109eed8f812\",\n"
                        + "  \"Message\" : \"Bad Request\",\n"
                        + "  \"Errors\" : [ {\n"
                        + "    \"ErrorCode\" : \"UK.HSBC.FailedEligibilityCheck\",\n"
                        + "    \"Message\" : \"Failed Eligibility check\"\n"
                        + "  } ]\n"
                        + "}";
        return objectMapper.readValue(jsonResponse, ErrorResponse.class);
    }

    private ErrorResponse getSampleErrorResponseWithNoErrors() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse =
                "{\n"
                        + "  \"Code\" : \"400\",\n"
                        + "  \"Id\" : \"cd22e7fb-4f0b-4f24-b2b3-f109eed8f812\",\n"
                        + "  \"Message\" : \"Bad Request\"\n"
                        + "}";
        return objectMapper.readValue(jsonResponse, ErrorResponse.class);
    }
}
