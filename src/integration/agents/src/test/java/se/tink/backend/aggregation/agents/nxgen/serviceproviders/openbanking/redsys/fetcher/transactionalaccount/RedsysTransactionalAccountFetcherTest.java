package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RedsysTransactionalAccountFetcherTest {
    private static final String SERVER_ERROR_RESPONSE =
            "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"server_error\",\"text\":\"server_error\"}]}";
    private static final String CONSENT_EXPIRED_RESPONSE =
            "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_EXPIRED\",\"text\":\"CONSENT_EXPIRED\"}]}";
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

    private void prepareErrorResponse(int httpStatus, String errorResponse) {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(httpStatus);
        when(httpResponse.getBody(String.class)).thenReturn(errorResponse);
        HttpResponseException hre = new HttpResponseException(null, httpResponse);
        when(apiClient.fetchTransactions(any(), any(), any())).thenThrow(hre);
    }
}
