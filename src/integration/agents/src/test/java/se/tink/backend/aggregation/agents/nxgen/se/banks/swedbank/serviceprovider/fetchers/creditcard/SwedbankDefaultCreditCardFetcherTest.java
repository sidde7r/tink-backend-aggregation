package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.rpc.DetailedCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultCreditCardFetcherTest {
    @Test
    public void fetchAccounts() {
        SwedbankDefaultApiClient apiClient = mock(SwedbankDefaultApiClient.class);
        BankProfile bankProfile = mock(BankProfile.class);
        when(apiClient.getBankProfiles()).thenReturn(Collections.singletonList(bankProfile));
        when(bankProfile.getEngagementOverViewResponse())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                SwedbankDefaultCreditCardFetcherTestData
                                        .ENGAGEMENT_OVERVIEW_RESPONSE,
                                EngagementOverviewResponse.class));
        when(apiClient.cardAccountDetails(any(LinkEntity.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                SwedbankDefaultCreditCardFetcherTestData
                                        .CREDIT_CARD_DETAILS_RESPONSE,
                                DetailedCardAccountResponse.class));

        final SwedbankDefaultCreditCardFetcher fetcher =
                new SwedbankDefaultCreditCardFetcher(apiClient, "SEK");

        final Collection<CreditCardAccount> accounts = fetcher.fetchAccounts();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        final CreditCardAccount cardAccount = accounts.iterator().next();
        assertEquals("1111 11** **** 1111", cardAccount.getAccountNumber());
        assertEquals("Betal- och kreditkort Mastercard Guld", cardAccount.getName());
        assertEquals("Sven Svensson", cardAccount.getHolderName().toString());
        assertEquals(-3295.04, cardAccount.getExactBalance().getDoubleValue(), 0.001);
        assertEquals(46704.96, cardAccount.getExactAvailableCredit().getDoubleValue(), 0.001);
    }
}
