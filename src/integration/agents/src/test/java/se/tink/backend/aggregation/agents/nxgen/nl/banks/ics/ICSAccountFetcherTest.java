package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSTimeProvider;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ICSAccountFetcherTest {

    private final LocalDateTimeSource localDateTimeSource = new ConstantLocalDateTimeSource();

    private final LocalDate today = localDateTimeSource.now(ZoneId.systemDefault()).toLocalDate();

    private ICSApiClient apiClient;

    private ICSAccountFetcher accountFetcher;

    @Before
    public void setUp() throws Exception {
        apiClient = mock(ICSApiClient.class);
        accountFetcher =
                new ICSAccountFetcher(
                        apiClient,
                        new ICSTimeProvider(localDateTimeSource, new PersistentStorage()));
    }

    @Test
    public void shouldGetHolderNameFromGivenResponse() {
        // when
        when(apiClient.getTransactionsByDate(TestHelper.ACCOUNT_ID, today.minusDays(30), today))
                .thenReturn(TestHelper.getCreditTransactionResponse());

        // then
        assertEquals(
                "P.C.A. ROSIER",
                ReflectionTestUtils.invokeMethod(
                        accountFetcher, "getHolderName", TestHelper.ACCOUNT_ID));
    }

    @Test
    public void shouldReturnNullHolderNameWhenResponseIsEmpty() {
        // when
        when(apiClient.getTransactionsByDate(TestHelper.ACCOUNT_ID, today.minusDays(30), today))
                .thenReturn(TestHelper.getEmptyCreditTransactionResponse());

        // then
        assertNull(
                ReflectionTestUtils.invokeMethod(
                        accountFetcher, "getHolderName", TestHelper.ACCOUNT_ID));
    }

    @Test
    public void shouldFetchAccountAndEnrichWithBalanceAndHolderName() {
        // given
        Collection<CreditCardAccount> creditCardAccounts = new ArrayList<>();
        creditCardAccounts.add(TestHelper.getCreditCardAccount());
        // when
        when(apiClient.getAccountBalance(TestHelper.ACCOUNT_ID))
                .thenReturn(TestHelper.getCreditBalanceResponse());
        when(apiClient.getAllAccounts()).thenReturn(TestHelper.getCreditAccountResponse());

        // then
        assertThat(accountFetcher.fetchAccounts())
                .usingRecursiveComparison()
                .isEqualTo(creditCardAccounts);
    }

    @Test
    public void shouldReturnEmptyListOfAccountsWhenResponseIsEmpty() {
        // when
        when(apiClient.getAllAccounts()).thenReturn(TestHelper.getEmptyCreditAccountResponse());

        // then
        assertEquals(Collections.EMPTY_LIST, accountFetcher.fetchAccounts());
    }
}
