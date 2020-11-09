package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.text.SimpleDateFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.SetupPinResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class SodexoApiClientTest {

    @Mock private RequestBuilder requestBuilder;
    @Mock private TinkHttpClient tinkHttpClient;
    @Mock private SodexoStorage sodexoStorage;
    @InjectMocks private SodexoApiClient sodexoApiClient;

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/sodexo/resource/";

    @Test
    public void shouldInitAuthenticate() {
        // given
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "authentication_response.json"),
                        AuthenticationResponse.class));

        // when
        AuthenticationResponse response =
                sodexoApiClient.authenticateWithCredentials("tink", "tinktink");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getSessionToken()).isEqualTo("session-token");
    }

    @Test
    public void shouldThrowWhenWhenAuthenticationFails() {
        // given
        mockThrowPostRequest(409);

        // when
        Throwable throwable =
                catchThrowable(() -> sodexoApiClient.authenticateWithCredentials("tink", "tinker"));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldResetPin() {
        // given
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "setup_pin.json"), SetupPinResponse.class));

        // when
        SetupPinResponse setupPinResponse = sodexoApiClient.setupNewPin("1111");

        // then
        assertThat(setupPinResponse.getMessage())
                .isEqualTo("O PIN da App foi definido com sucesso.");
    }

    @Test
    public void shouldAuthenticateWithPIN() {
        // given
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "authentication_response.json"),
                        AuthenticationResponse.class));

        // when
        AuthenticationResponse authenticationResponse = sodexoApiClient.authenticateWithPin("1111");

        // then
        assertThat(authenticationResponse).isNotNull();
        assertThat(authenticationResponse.getSessionToken()).isEqualTo("session-token");
    }

    @Test
    public void shouldGetBalanceResponse() {
        // given
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "balances_response.json"), BalanceResponse.class));

        // when
        BalanceResponse balanceResponse = sodexoApiClient.getBalanceResponse();

        // then
        assertThat(balanceResponse).isNotNull();
        assertThat(balanceResponse.getBalance()).isEqualTo(882);
        assertThat(balanceResponse.getStamp()).isEqualTo("14:51");
        assertThat(balanceResponse.getRealtime()).isEqualTo("1");
        assertThat(balanceResponse.getUsage()).isEqualTo("655.23");
        assertThat(balanceResponse.getBenefitDate()).isEqualTo("27 NOVEMBRO");
    }

    @Test
    public void shouldGetTransactionResponse() {
        // given
        mockPostRequest(
                SerializationUtils.deserializeFromString(
                        new File(RESOURCE_PATH + "transaction_response.json"),
                        TransactionResponse.class));

        // when
        TransactionResponse transactionResponse = sodexoApiClient.getTransactions();

        // then
        assertThat(transactionResponse).isNotNull();
        assertThat(transactionResponse.getTransactions()).hasSize(6);
        assertThat(transactionResponse.getTransactions().get(0).getDescription())
                .isEqualTo("CARREGAMENTO DE BENEF\u00cdCIO");
        assertThat(transactionResponse.getTransactions().get(0).getDate()).isEqualTo("15.10");
        assertThat(
                        new SimpleDateFormat("yyy-MM-dd")
                                .format(transactionResponse.getTransactions().get(0).getDateIso()))
                .isEqualTo("2020-10-15");
        assertThat(transactionResponse.getTransactions().get(0).getType()).isEqualTo(1);
        assertThat(transactionResponse.getTransactions().get(0).getAmount()).isEqualTo(147);
    }

    private <T> void mockPostRequest(T response) {
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.post(any(), any())).thenReturn(response);
        when(requestBuilder.post(any())).thenReturn(response);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
    }

    private void mockThrowPostRequest(int status) {
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(status);
        when(requestBuilder.post(any(), any()))
                .thenThrow(new HttpResponseException(null, httpResponse));
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
    }
}
