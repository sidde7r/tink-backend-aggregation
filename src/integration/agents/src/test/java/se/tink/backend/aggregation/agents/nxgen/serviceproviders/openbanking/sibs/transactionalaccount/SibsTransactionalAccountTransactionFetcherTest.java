package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

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
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsTransactionalAccountTransactionFetcherTest {

    private SibsUserState userState;
    private Consent consent;
    private SibsTransactionalAccountTransactionFetcher objectUnderTest;
    private CredentialsRequest credentialsRequest;
    private SibsBaseApiClient sibsBaseApiClient;
    private se.tink.backend.agents.rpc.Account rpcAccount;
    private Account account;

    @Before
    public void init() {
        userState = Mockito.mock(SibsUserState.class);
        consent = Mockito.mock(Consent.class);
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(false);
        Mockito.when(userState.getConsent()).thenReturn(consent);
        sibsBaseApiClient = Mockito.mock(SibsBaseApiClient.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        objectUnderTest =
                new SibsTransactionalAccountTransactionFetcher(
                        sibsBaseApiClient, credentialsRequest, userState);
        rpcAccount = Mockito.mock(se.tink.backend.agents.rpc.Account.class);
        account = Mockito.mock(Account.class);
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(Lists.newArrayList(rpcAccount));
    }

    @Test
    public void
            getTransactionsFetchBeginDateShouldReturnTheBeginOfTheWordDateWhenCertainDateIsNull() {
        // given
        final String accountId = "accountId";
        Mockito.when(rpcAccount.getCertainDate()).thenReturn(null);
        Mockito.when(rpcAccount.getBankId()).thenReturn(accountId);
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        // when
        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(1970, result.getYear());
        Assert.assertEquals(1, result.getMonthValue());
        Assert.assertEquals(1, result.getDayOfMonth());
    }

    @Test
    public void
            getTransactionsFetchBeginDateShouldReturnTheBeginOfTheWordDateWhenThereIsNoRpcAccount() {
        final String accountId = "accountId";
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(Collections.emptyList());
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        // when
        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(1970, result.getYear());
        Assert.assertEquals(1, result.getMonthValue());
        Assert.assertEquals(1, result.getDayOfMonth());
    }

    @Test
    public void getTransactionsFetchBeginDateShouldReturnCertainDate() {
        // given
        final String accountId = "accountId";
        LocalDate expectedDate = LocalDate.now().minusDays(88);
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(true);
        Mockito.when(rpcAccount.getCertainDate())
                .thenReturn(
                        Date.from(expectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        Mockito.when(rpcAccount.getBankId()).thenReturn(accountId);
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        // when
        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(expectedDate.getYear(), result.getYear());
        Assert.assertEquals(expectedDate.getMonthValue(), result.getMonthValue());
        Assert.assertEquals(expectedDate.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    public void
            getTransactionsFetchBeginDateShouldReturnNowMinus89DaysDateWhenTheConsentIsOlderThen90Days() {
        final String accountId = "accountId";
        Mockito.when(consent.isConsentOlderThan30Minutes()).thenReturn(true);
        Mockito.when(rpcAccount.getCertainDate()).thenReturn(null);
        Mockito.when(rpcAccount.getBankId()).thenReturn(accountId);
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        LocalDate expectedDate = LocalDate.now().minusDays(89);
        // when
        LocalDate result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(expectedDate.getYear(), result.getYear());
        Assert.assertEquals(expectedDate.getMonthValue(), result.getMonthValue());
        Assert.assertEquals(expectedDate.getDayOfMonth(), result.getDayOfMonth());
    }
}
