package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsUserStateTest {

    private SibsUserState objectUnderTest;
    private PersistentStorage persistentStorage;
    private Credentials credentials;

    @Before
    public void init() {
        persistentStorage = Mockito.mock(PersistentStorage.class);
        credentials = Mockito.mock(Credentials.class);
        objectUnderTest = new SibsUserState(persistentStorage, credentials);
    }

    @Test
    public void
            getTransactionsFetchBeginDateShouldReturnTheBeginOfTheWordDateWhenUpdatedDateIsNull() {
        // given
        Account account = Mockito.mock(Account.class);
        Mockito.when(credentials.getUpdated()).thenReturn(null);
        // when
        String result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(result, "1970-01-01");
    }

    @Test
    public void
            getTransactionsFetchBeginDateShouldReturnTheBeginOfTheWordDateWhenUpdatedIsBeginOfTheWord() {
        // given
        Account account = Mockito.mock(Account.class);
        Mockito.when(credentials.getUpdated()).thenReturn(new Date(0));
        // when
        String result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(result, "1970-01-01");
    }

    @Test
    public void getTransactionsFetchBeginDateShouldReturnDateTenDaysBack() {
        // given
        Account account = Mockito.mock(Account.class);
        LocalDate localDate = LocalDate.of(2019, 06, 11);
        Mockito.when(credentials.getUpdated())
                .thenReturn(
                        Date.from(
                                localDate
                                        .atStartOfDay()
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()));
        // when
        String result = objectUnderTest.getTransactionsFetchBeginDate(account);
        // then
        Assert.assertEquals(result, "2019-06-01");
    }
}
