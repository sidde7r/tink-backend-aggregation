package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator;

import java.time.LocalDate;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.SavingsAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc.CustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc.ErrorStatusResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class VolvoFinansBankIdAutenticatorTest {
    private static String ACCOUNT_ID = "1234567890";

    private VolvoFinansBankIdAutenticator authenticator;
    private VolvoFinansApiClient apiClient;
    private HttpResponse httpResponseBankServiceClosed;
    private HttpResponse httpResponseNotFound;

    @Before
    public void setUp() throws Exception {
        apiClient = Mockito.mock(VolvoFinansApiClient.class);

        // collect call to bank
        AuthenticateResponse authResponse = Mockito.mock(AuthenticateResponse.class);
        when(authResponse.getBankIdStatus()).thenReturn(BankIdStatus.DONE);
        when(apiClient.loginBankIdPoll(anyString())).thenReturn(authResponse);

        // fetch credit card accounts
        when(apiClient.creditCardAccounts()).thenReturn(creditCardResponse);

        // setup authenticator
        authenticator = new VolvoFinansBankIdAutenticator(apiClient, new SessionStorage());

        // not found exception
        httpResponseNotFound = Mockito.mock(HttpResponse.class);
        when(httpResponseNotFound.getStatus()).thenReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

        // bank service closed exception
        httpResponseBankServiceClosed = Mockito.mock(HttpResponse.class);
        when(httpResponseBankServiceClosed.getStatus()).thenReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);
        when(httpResponseBankServiceClosed.hasBody()).thenReturn(true);
        when(httpResponseBankServiceClosed.getBody(ErrorStatusResponse.class)).thenReturn(errorStatusResponse);
    }

    @Test(expected = BankServiceException.class)
    public void collect_CreditCard_BankService_Closed() throws Exception {
        // keep alive call
        when(apiClient.keepAlive()).thenReturn(customerResponseCreditCard);

        // fetch transactions
        HttpResponseException hre = new HttpResponseException(null, httpResponseBankServiceClosed);
        when(apiClient.creditCardAccountTransactions(anyString(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())).thenThrow(hre);

        BankIdStatus status = authenticator.collect("identificationId");
    }

    @Test
    public void collect_CreditCard_OK() throws Exception {
        // keep alive call
        when(apiClient.keepAlive()).thenReturn(customerResponseCreditCard);

        // fetch credit card transactions
        when(apiClient.creditCardAccountTransactions(anyString(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())).thenReturn(new CreditCardTransactionsResponse());

        BankIdStatus status = authenticator.collect("identificationId");
        assertEquals(status, BankIdStatus.DONE);
    }

    @Test(expected = BankServiceException.class)
    public void collect_Savings_BankService_Closed() throws Exception {
        // keep alive call
        when(apiClient.keepAlive()).thenReturn(customerResponseSavings);

        // fetch savings accounts
        when(apiClient.savingsAccounts()).thenReturn(savingsAccountsResponse);

        // fetch savings transactions
        HttpResponseException hre = new HttpResponseException(null, httpResponseBankServiceClosed);
        when(apiClient.savingsAccountTransactions(anyString(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())).thenThrow(hre);

        BankIdStatus status = authenticator.collect("identificationId");
    }

    @Test
    public void collect_Savings_NotFound_OK() throws Exception {
        // keep alive call
        when(apiClient.keepAlive()).thenReturn(customerResponseSavings);

        // fetch savings accounts
        HttpResponseException hre = new HttpResponseException(null, httpResponseNotFound);
        when(apiClient.savingsAccounts()).thenThrow(hre);

        BankIdStatus status = authenticator.collect("identificationId");
        assertEquals(status, BankIdStatus.DONE);
    }

    @Test
    public void collect_Savings_OK() throws Exception {
        // keep alive call
        when(apiClient.keepAlive()).thenReturn(customerResponseSavings);

        // fetch savings accounts
        when(apiClient.savingsAccounts()).thenReturn(savingsAccountsResponse);

        // fetch savings transactions
        when(apiClient.savingsAccountTransactions(anyString(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())).thenReturn(new AccountTransactionsResponse());

        BankIdStatus status = authenticator.collect("identificationId");
        assertEquals(status, BankIdStatus.DONE);
    }

    static CustomerResponse customerResponseCreditCard = SerializationUtils.deserializeFromString("{\"namn\":\"KALLE KULA\",\"kort\":true,\"spar\":false,\"finansiering\":true,\"aterforsaljare\":null}", CustomerResponse.class);
    static CustomerResponse customerResponseSavings = SerializationUtils.deserializeFromString("{\"namn\":\"KALLE KULA\",\"kort\":false,\"spar\":true,\"finansiering\":true,\"aterforsaljare\":null}", CustomerResponse.class);
    static CreditCardsResponse creditCardResponse = SerializationUtils.deserializeFromString("[{\"kontoId\":\"" + ACCOUNT_ID + "\"}]", CreditCardsResponse.class);
    static SavingsAccountsResponse savingsAccountsResponse = SerializationUtils.deserializeFromString("[{\"kontoId\":\"" + ACCOUNT_ID + "\"}]", SavingsAccountsResponse.class);
    static ErrorStatusResponse errorStatusResponse = SerializationUtils.deserializeFromString("{\"status\":\"STANGD\",\"meddelande\":\"Tjänsten är tillfälligt stängd för underhåll och beräknas öppna igen klockan 24.00.\"}", ErrorStatusResponse.class);

}
