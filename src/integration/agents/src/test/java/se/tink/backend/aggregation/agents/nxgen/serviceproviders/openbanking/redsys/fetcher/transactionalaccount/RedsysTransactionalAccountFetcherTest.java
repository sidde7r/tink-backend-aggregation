package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RedsysTransactionalAccountFetcherTest {

    private static final String SERVER_ERROR_RESPONSE =
            "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"server_error\",\"text\":\"server_error\"}]}";
    private static final String CONSENT_EXPIRED_RESPONSE =
            "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_EXPIRED\",\"text\":\"CONSENT_EXPIRED\"}]}";
    private static final String ACCOUNT_RESPONSE =
            "{\"accounts\":[{\"resourceId\":\"ES018202000000000000000000000222222\",\"iban\":\"ES1714658336187317761933\",\"currency\":\"EUR\",\"ownerName\":\"JOHN DOE\",\"status\":\"enabled\",\"product\":\"CUENTA ONLINE\",\"balances\":[{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1543.71\"},\"balanceType\":\"closingBooked\"},{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1543.71\"},\"balanceType\":\"interimAvailable\"}],\"_links\":{\"balances\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/balances\"},\"transactions\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/transactions?withBalance=true&bookingStatus=both\"}}},{\"resourceId\":\"ES018202000000000000000000000222222\",\"iban\":\"ES1714658336187317761933\",\"currency\":\"EUR\",\"ownerName\":\"JOHN DOE\",\"status\":\"enabled\",\"product\":\"CUENTA VA CONTIGO\",\"balances\":[{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1440.30\"},\"balanceType\":\"closingBooked\"},{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1440.30\"},\"balanceType\":\"interimAvailable\"}],\"_links\":{\"balances\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/balances\"},\"transactions\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/transactions?withBalance=true&bookingStatus=both\"}}}]}";
    private RedsysTransactionalAccountFetcher accountFetcher;
    private RedsysApiClient apiClient;
    private RedsysConsentController consentController;

    @Before
    public void setup() {
        apiClient = mock(RedsysApiClient.class);
        consentController = mock(RedsysConsentController.class);
        accountFetcher =
                new RedsysTransactionalAccountFetcher(
                        apiClient,
                        mock(RedsysConsentController.class),
                        mock(AspspConfiguration.class));
    }

    @Test(expected = SessionException.class)
    public void shouldThrowSessionExceptionWhenConsentRevoked() {
        // given
        when(consentController.getConsentId()).thenReturn("DUMMY");
        when(consentController.requestConsent()).thenReturn(true);
        prepareErrorResponse(409, CONSENT_EXPIRED_RESPONSE);

        // when
        accountFetcher.getTransactionsFor(mock(TransactionalAccount.class), null);
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankServiceExceptionWhenServerError() {
        // given
        when(consentController.getConsentId()).thenReturn("DUMMY");
        prepareErrorResponse(400, SERVER_ERROR_RESPONSE);

        // when
        accountFetcher.getTransactionsFor(
                mock(TransactionalAccount.class), mock(PaginationKey.class));
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // given
        when(apiClient.fetchAccounts(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                ACCOUNT_RESPONSE, ListAccountsResponse.class));
        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertFirstAccountValid(iterator.next());
    }

    private void assertFirstAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1543.71"));
        assertThat(account.getIdModule().getAccountName()).isEqualTo("CUENTA ONLINE");
        assertThat(account.getIdModule().getAccountNumber())
                .isEqualTo("ES17 1465 8336 1873 1776 1933");
        assertThat(account.getParties().get(0).getName()).isEqualTo("John Doe");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(HOLDER);
    }

    private void prepareErrorResponse(int httpStatus, String errorResponse) {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(httpStatus);
        when(httpResponse.getBody(String.class)).thenReturn(errorResponse);
        HttpResponseException hre = new HttpResponseException(null, httpResponse);
        when(apiClient.fetchTransactions(any(), any(), any())).thenThrow(hre);
    }
}
