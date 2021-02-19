package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.rpc.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@RequiredArgsConstructor
public class NordeaDkApiClientMockWrapper {

    private final NordeaDkApiClient mockApiClient;

    public void mockGetAccountsUsingFile(String filePath) {
        when(mockApiClient.getAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), AccountsResponse.class));
    }

    public void mockFetchInvestmentsUsingFile(String filePath) {
        when(mockApiClient.fetchInvestments())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), CustodyAccountsResponse.class));
    }

    public void mockFetchCreditCardsUsingFile(String filePath) {
        when(mockApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), CreditCardsResponse.class));
    }

    public void mockFetchCreditCardDetailsUsingFile(String cardId, String filePath) {
        when(mockApiClient.fetchCreditCardDetails(contains(cardId)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), CreditCardDetailsResponse.class));
    }

    public void mockFetchCreditCardTransactionsPageUsingFile(
            String cardId, int pageNumber, String filePath) {
        when(mockApiClient.fetchCreditCardTransactions(cardId, pageNumber))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), CreditCardTransactionsResponse.class));
    }
}
