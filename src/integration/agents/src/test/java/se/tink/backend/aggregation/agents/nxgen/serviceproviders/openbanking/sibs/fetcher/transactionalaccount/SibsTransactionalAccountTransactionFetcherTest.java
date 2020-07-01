package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.Consent;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsTransactionalAccountTransactionFetcherTest {

    private Consent consent;
    private SibsTransactionalAccountTransactionFetcher objectUnderTest;
    private CredentialsRequest credentialsRequest;
    private se.tink.backend.agents.rpc.Account rpcAccount;
    private Account account;
    private static final String ACCOUNT_ID = "dummyAccountId";
    private static final LocalDate BIG_BANG_DATE =
            SibsTransactionalAccountTransactionFetcher.BIG_BANG_DATE;
    private static final LocalDate DAYS_BACK_90 =
            LocalDate.now()
                    .minusDays(
                            SibsTransactionalAccountTransactionFetcher
                                    .DAYS_BACK_TO_FETCH_TRANSACTIONS_WHEN_CONSENT_OLD);

    @Before
    public void init() {
        SibsUserState userState = Mockito.mock(SibsUserState.class);
        consent = Mockito.mock(Consent.class);
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(false);
        Mockito.when(userState.getConsent()).thenReturn(consent);
        SibsBaseApiClient sibsBaseApiClient = Mockito.mock(SibsBaseApiClient.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        objectUnderTest =
                new SibsTransactionalAccountTransactionFetcher(
                        sibsBaseApiClient, credentialsRequest, userState);
        rpcAccount = Mockito.mock(se.tink.backend.agents.rpc.Account.class);
        account = Mockito.mock(Account.class);
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(Lists.newArrayList(rpcAccount));
        Mockito.when(rpcAccount.getBankId()).thenReturn(ACCOUNT_ID);
        Mockito.when(account.isUniqueIdentifierEqual(ACCOUNT_ID)).thenReturn(true);
    }

    @Test
    public void shouldReturnBigBangDateWhenCertainDateIsNull() {
        Mockito.when(rpcAccount.getCertainDate()).thenReturn(null);

        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);

        Assert.assertEquals(BIG_BANG_DATE, result);
    }

    @Test
    public void shouldReturnBigBangDateWhenThereIsNoRpcAccount() {
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(Collections.emptyList());

        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);

        Assert.assertEquals(BIG_BANG_DATE, result);
    }

    @Test
    public void shouldReturnFromCertainDateWhenConsentIsOlderThan30Minutes() {
        LocalDate expectedDate = LocalDate.now().minusDays(88);
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(true);
        Mockito.when(rpcAccount.getCertainDate())
                .thenReturn(
                        Date.from(expectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);

        Assert.assertEquals(expectedDate, result);
    }

    @Test
    public void shouldReturn89DaysBackDateWhenCertainDateIsNullAndConsentsAreOlderThan30Minutes() {
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(true);
        Mockito.when(rpcAccount.getCertainDate()).thenReturn(null);

        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);

        Assert.assertEquals(DAYS_BACK_90, result);
    }

    @Test
    public void
            shouldReturn89DaysBackDateWhenCertainDateIsOlderThan90DaysAndConsentsAreOlderThan30Minutes() {
        Date moreThan90CertainDateDays =
                Date.from(
                        DAYS_BACK_90.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(true);
        Mockito.when(rpcAccount.getCertainDate()).thenReturn(moreThan90CertainDateDays);

        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);

        Assert.assertEquals(DAYS_BACK_90, result);
    }
}
