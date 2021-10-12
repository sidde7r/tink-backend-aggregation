package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AuthSessionStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.SessionAccountPair;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.PensionDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountsOverviewResponse;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AvanzaBankIdAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/brokers/avanza/resources/";

    AvanzaBankIdAuthenticator authenticator;
    AvanzaApiClient apiClient;
    AuthSessionStorageHelper authSessionStorage;
    TemporaryStorage temporaryStorage;
    List<SessionAccountPair> sessionAccountPairs;

    HttpRequest httpRequestMocked = mock(HttpRequest.class);
    HttpResponse httpResponseMocked = mock(HttpResponse.class);
    HttpResponseException exception =
            new HttpResponseException(httpRequestMocked, httpResponseMocked);
    ErrorResponse errorResponse = mock(ErrorResponse.class);
    String authSessionValue;

    @Before
    public void setup() {
        SessionStorage sessionStorage = new SessionStorage();
        temporaryStorage = new TemporaryStorage();
        this.authSessionStorage = mock(AuthSessionStorageHelper.class);
        this.apiClient = mock(AvanzaApiClient.class);
        this.authenticator =
                new AvanzaBankIdAuthenticator(
                        apiClient, authSessionStorage, temporaryStorage, sessionStorage);
        sessionAccountPairs = getSessionAccountPairs();
        String authSessionValue = "dummyStringValue";
    }

    @Test
    public void shouldThrowBankIdErrorAlreadyInProgress() {

        // when
        when(exception.getResponse().getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        // then
        assertThatThrownBy(() -> authenticator.handleInitBankIdErrors(exception))
                .isInstanceOf(BankIdError.ALREADY_IN_PROGRESS.exception().getClass());
    }

    @Test
    public void shouldThrowBankServiceErrorBankSideFailureInMethodHandleInitBankIdErrors() {

        // when
        when(exception.getResponse().getStatus()).thenReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

        // then
        assertThatThrownBy(() -> authenticator.handleInitBankIdErrors(exception))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldThrowBankServiceErrorBankSideFailureInMethodHandlePollBankIdErrors() {

        // when
        when(exception.getResponse().getStatus()).thenReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

        // then
        assertThatThrownBy(() -> authenticator.handlePollBankIdErrors(exception))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldThrowBankIdErrorCancelled() {

        // when
        when(exception.getResponse().hasBody()).thenReturn(true);
        when(exception.getResponse().getBody(ErrorResponse.class)).thenReturn(errorResponse);
        when(errorResponse.isUserCancel()).thenReturn(true);

        // then
        assertThatThrownBy(() -> authenticator.handlePollBankIdErrors(exception))
                .isInstanceOf(BankIdError.CANCELLED.exception().getClass());
    }

    @Test
    public void shouldThrowBankIdErrorTimeout() {

        // when
        when(exception.getResponse().hasBody()).thenReturn(true);
        when(exception.getResponse().getBody(ErrorResponse.class)).thenReturn(errorResponse);
        when(errorResponse.isBankIdTimeout()).thenReturn(true);

        // then
        assertThatThrownBy(() -> authenticator.handlePollBankIdErrors(exception))
                .isInstanceOf(BankIdError.TIMEOUT.exception().getClass());
    }

    @Test
    public void shouldReturnCorrectHolderNameValue() {

        // when
        when(apiClient.fetchHolderNameFromPensionDetails(any(), any()))
                .thenReturn(getPensionDetailResponse());

        // then
        authenticator.storeHolderNameIfAvailable(sessionAccountPairs);
        assertThat(temporaryStorage.get(StorageKeys.HOLDER_NAME)).isEqualTo("Firstname Surname");
    }

    @Test
    public void shouldReturnLoginErrorNotCustomerException() {

        // given
        BankIdInitResponse bankIdInitResponse = getBankIdInitResponse();
        String transactionId = bankIdInitResponse.getTransactionId();
        BankIdCollectResponse bankIdResponse = getEmptyLoginsBankIdCollectResponse();

        // when
        when(apiClient.collectBankId(transactionId)).thenReturn(bankIdResponse);

        // then
        assertThatThrownBy(() -> authenticator.collect(bankIdInitResponse))
                .isInstanceOf(LoginError.NOT_CUSTOMER.exception().getClass());
        assertEquals(
                "You don't have any commitments in the selected bank.",
                LoginError.NOT_CUSTOMER.exception().getError().userMessage().get());
    }

    @Test
    public void shouldReturnBankIdCollectResponseFromCollectMethod() {

        // given
        BankIdInitResponse bankIdInitResponse = getBankIdInitResponse();
        String transactionId = bankIdInitResponse.getTransactionId();
        BankIdCollectResponse bankIdResponse = getBankIdCollectResponse();

        // when
        when(apiClient.collectBankId(transactionId)).thenReturn(bankIdResponse);

        // then
        assertEquals(bankIdResponse.getBankIdStatus(), authenticator.collect(bankIdInitResponse));
    }

    @Test
    public void shouldReturnListOfSessionAccountPairs() {
        // when
        when(apiClient.fetchAccounts(authSessionValue)).thenReturn(getAccountsOverviewResponse());

        // then
        assertEquals(
                sessionAccountPairs,
                authenticator
                        .getSessionAccountPairs()
                        .apply(authSessionValue)
                        .collect(Collectors.toList()));
    }

    private List<SessionAccountPair> getSessionAccountPairs() {
        sessionAccountPairs = new ArrayList<>();
        sessionAccountPairs.add(new SessionAccountPair(authSessionValue, "1000075"));
        sessionAccountPairs.add(new SessionAccountPair(authSessionValue, "1000011"));
        sessionAccountPairs.add(new SessionAccountPair(authSessionValue, "1000093"));
        return sessionAccountPairs;
    }

    private AccountsOverviewResponse getAccountsOverviewResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "accountoverviewresponse.json").toFile(),
                AccountsOverviewResponse.class);
    }

    private BankIdCollectResponse getBankIdCollectResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "bankidcollectresponse.json").toFile(),
                BankIdCollectResponse.class);
    }

    private BankIdCollectResponse getEmptyLoginsBankIdCollectResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"name\":\"firstName\",\"logins\":[],\"transactionId\":\"transactionId\",\"recommendedTargetCustomers\":[],\"state\":\"COMPLETE\"}",
                BankIdCollectResponse.class);
    }

    private BankIdInitResponse getBankIdInitResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"transactionId\":\"transactionId\",\"expires\":\"2020-07-21T09:18:18.409\",\"autostartToken\":\"autoStartToken\"}",
                BankIdInitResponse.class);
    }

    private PensionDetailResponse getPensionDetailResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "pensiondetailresponse.json").toFile(),
                PensionDetailResponse.class);
    }
}
