package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class ICSAccountFetcherTest {

    private ICSApiClient apiClient;
    private ICSAccountFetcher icsAccountFetcher;

    @Before
    public void setUp() throws Exception {
        apiClient = mock(ICSApiClient.class);
        icsAccountFetcher = new ICSAccountFetcher(apiClient);
    }

    @Test
    public void shouldGetHolderNameFromGivenResponse() {
        // when
        when(apiClient.getTransactionsByDate(
                        TestHelper.ACCOUNT_ID, LocalDate.now().minusDays(30), LocalDate.now()))
                .thenReturn(TestHelper.getCreditTransactionResponse());

        // then
        assertEquals(
                "P.C.A. ROSIER",
                ReflectionTestUtils.invokeMethod(
                        icsAccountFetcher, "getHolderName", TestHelper.ACCOUNT_ID));
    }

    @Test
    public void shouldReturnNullHolderNameWhenResponseIsEmpty() {
        // when
        when(apiClient.getTransactionsByDate(
                        TestHelper.ACCOUNT_ID, LocalDate.now().minusDays(30), LocalDate.now()))
                .thenReturn(TestHelper.getEmptyCreditTransactionResponse());

        // then
        assertNull(
                ReflectionTestUtils.invokeMethod(
                        icsAccountFetcher, "getHolderName", TestHelper.ACCOUNT_ID));
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
        assertThat(icsAccountFetcher.fetchAccounts())
                .usingRecursiveComparison()
                .isEqualTo(creditCardAccounts);
    }

    @Test
    public void shouldReturnEmptyListOfAccountsWhenResponseIsEmpty() {
        // when
        when(apiClient.getAllAccounts()).thenReturn(TestHelper.getEmptyCreditAccountResponse());

        // then
        assertEquals(Collections.EMPTY_LIST, icsAccountFetcher.fetchAccounts());
    }
}
