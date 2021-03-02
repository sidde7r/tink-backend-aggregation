package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.SetPinResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.CardListResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class EdenredApiClientTest {

    @Mock private TinkHttpClient httpClient;

    @Mock private EdenredStorage edenredStorage;

    @InjectMocks private EdenredApiClient edenredApiClient;

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/edenred/resources/";

    @Test
    public void shouldAuthenticateDefault() {
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "authentication-default.json"),
                        AuthenticationResponse.class));

        AuthenticationResponse response =
                edenredApiClient.authenticateDefault("tinker", "tinktink");

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getToken()).isEqualTo("super-token");
    }

    @Test
    public void shouldThrowWhenAuthenticateDefault() {
        mockPostRequestThrow(409);

        Throwable throwable =
                catchThrowable(() -> edenredApiClient.authenticateDefault("tinker", "tinktink"));

        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldSetupPin() throws IOException {
        mockPostRequest(
                FileUtils.readFileToString(new File(RESOURCE_PATH + "setup-pin.json"), "UTF-8"));
        when(edenredStorage.getToken()).thenReturn("a-token");

        SetPinResponse response = edenredApiClient.setupPin("tinker", "1234");

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getKey()).isEqualTo("tokenRememberMe");
        assertThat(response.getData().getValue()).isEqualTo("df147322-6fe1-49dd-a6b8-4fd071e18122");
    }

    @Test
    public void shouldAuthenticatePin() {
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "authentication-pin.json"),
                        AuthenticationResponse.class));

        AuthenticationResponse response =
                edenredApiClient.authenticatePin("tink-1234-tink", "1234");

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getToken()).isEqualTo("super-pin-token");
    }

    @Test
    public void shouldGetCards() {
        mockGetRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "card-list.json"), CardListResponse.class));
        when(edenredStorage.getToken()).thenReturn("a-token");

        CardListResponse response = edenredApiClient.getCards();

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull().hasSize(1);
        assertThat(response.getData().get(0)).isNotNull();
        assertThat(response.getData().get(0).getId()).isEqualTo(123456L);
        assertThat(response.getData().get(0).getNumber()).isEqualTo("4331230107896123");
    }

    @Test
    public void shouldGetTransactions() {
        mockGetRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "transactions.json"), TransactionsResponse.class));
        when(edenredStorage.getToken()).thenReturn("a-token");

        TransactionsResponse response = edenredApiClient.getTransactions(1234L);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAccount()).isNotNull();
        assertThat(response.getData().getMovementList()).isNotNull().hasSize(1);
        assertThat(response.getData().getMovementList().get(0).getAmount()).isEqualTo(-13.48);
        assertThat(response.getData().getMovementList().get(0).getTransactionName()).isNotNull();
    }

    private <T> void mockPostRequest(T response) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any())).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);
        when(httpClient.request(any(URL.class))).thenReturn(requestBuilder);
    }

    private void mockPostRequestThrow(int status) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any())).thenReturn(requestBuilder);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(status);
        when(requestBuilder.post(any())).thenThrow(new HttpResponseException(null, response));
        when(httpClient.request(any(URL.class))).thenReturn(requestBuilder);
    }

    private <T> void mockGetRequest(T response) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.get(any())).thenReturn(response);
        when(httpClient.request(any(URL.class))).thenReturn(requestBuilder);
    }
}
