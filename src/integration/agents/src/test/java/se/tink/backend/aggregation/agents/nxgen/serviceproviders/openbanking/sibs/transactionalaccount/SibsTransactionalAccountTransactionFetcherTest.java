package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SibsTransactionalAccountTransactionFetcherTest {
    private SibsUserState userState = Mockito.mock(SibsUserState.class);
    private Consent consent = Mockito.mock(Consent.class);
    private SibsTransactionalAccountTransactionFetcher objectUnderTest;
    private SibsBaseApiClient sibsBaseApiClient = Mockito.mock(SibsBaseApiClient.class);
    private TransactionalAccount account = Mockito.mock(TransactionalAccount.class);

    @Before
    public void init() {
        Mockito.when(userState.getConsent()).thenReturn(consent);
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(false);
        objectUnderTest =
                new SibsTransactionalAccountTransactionFetcher(sibsBaseApiClient, userState);
    }

    @Test
    public void
            ensureApiClient_isCalled_whenConsent_isNewerThan30Minutes_andFromDate_isOlderThan3Months() {
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(false);

        objectUnderTest.fetchInitialTransactionsFor(account, LocalDate.of(1970, 1, 1));

        Mockito.verify(sibsBaseApiClient, Mockito.times(1))
                .getAccountTransactions(Mockito.eq(account), Mockito.any());
    }

    @Test
    public void
            ensureAuthorizationExceptionIsThrown_whenConsent_isOlderThan30Minutes_andFromDate_isOlderThan3Months() {
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(true);

        objectUnderTest.fetchInitialTransactionsFor(account, LocalDate.of(1970, 1, 1));

        Mockito.verify(sibsBaseApiClient, Mockito.times(1))
                .getAccountTransactions(
                        account,
                        SibsTransactionalAccountTransactionFetcher.getOldestAllowedFromDate());
    }
}
