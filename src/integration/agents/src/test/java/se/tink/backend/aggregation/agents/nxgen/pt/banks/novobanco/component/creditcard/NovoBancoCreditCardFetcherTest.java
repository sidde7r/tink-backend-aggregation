package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.creditcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient.CreditCardAggregatedData;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.creditcard.detail.CreditCardData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.CreditCardDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetCreditCardDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NovoBancoCreditCardFetcherTest {
    @Test
    public void shouldReturnEmptyCollectionIfNoCreditCardsAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        CreditCardAggregatedData response =
                new CreditCardAggregatedData(Collections.emptyList(), Collections.emptyList());
        when(apiClient.getCreditCards()).thenReturn(response);
        NovoBancoCreditCardFetcher fetcher = new NovoBancoCreditCardFetcher(apiClient);

        // when
        Collection<CreditCardAccount> accounts = fetcher.fetchAccounts();

        // then
        assertTrue(accounts.isEmpty());
    }

    @Test
    public void shouldReturnNonEmptyCollectionIfCreditCardsAvailable() {
        GetCreditCardDetailsResponse resp;
        CreditCardAggregatedData response =
                new CreditCardAggregatedData(
                        CreditCardData.getCreditCards(), CreditCardData.getAccounts());
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getCreditCards()).thenReturn(response);
        NovoBancoCreditCardFetcher fetcher = new NovoBancoCreditCardFetcher(apiClient);

        // when & then
        assertFalse(fetcher.fetchAccounts().isEmpty());
    }

    @Test
    public void shouldReturnCorrectlyMappedAccounts() {
        CreditCardAggregatedData response =
                new CreditCardAggregatedData(
                        CreditCardData.getCreditCards(), CreditCardData.getAccounts());
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getCreditCards()).thenReturn(response);
        NovoBancoCreditCardFetcher fetcher = new NovoBancoCreditCardFetcher(apiClient);

        // when
        Collection<CreditCardAccount> accounts = fetcher.fetchAccounts();

        Collection<CreditCardDto> referenceCreditCardDtos =
                CreditCardData.getReferenceCreditCardDtos();

        // then
        assertEquals(referenceCreditCardDtos.size(), accounts.size());
        assertAccountsEqual(referenceCreditCardDtos, accounts);
        assertFalse(accounts.isEmpty());
    }

    private void assertAccountsEqual(
            Collection<CreditCardDto> referenceCreditCardDtos,
            Collection<CreditCardAccount> accounts) {
        referenceCreditCardDtos.forEach(
                refCC -> {
                    CreditCardAccount account =
                            findMatchingAccount(accounts, refCC.getUniqueIdentifier());
                    assertNotNull("No account matching reference Unique Identifier found", account);
                    assertEquals(refCC.getAccountName(), account.getName());
                    assertEquals(refCC.getAccountNumber(), account.getAccountNumber());
                    assertTrue(account.isUniqueIdentifierEqual(refCC.getUniqueIdentifier()));
                    assertEquals(refCC.getAvailableCredit(), account.getExactAvailableCredit());
                    assertEquals(refCC.getBalance(), account.getExactBalance());
                });
    }

    private CreditCardAccount findMatchingAccount(
            Collection<CreditCardAccount> accounts, String uniqueIdentifier) {
        return accounts.stream()
                .filter(acc -> acc.isUniqueIdentifierEqual(uniqueIdentifier))
                .findFirst()
                .orElse(null);
    }
}
