package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.rpc.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaDkTestUtils {

    public static NordeaDkApiClient mockApiClient() {
        NordeaDkApiClient client = mock(NordeaDkApiClient.class);

        when(client.getAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_ACCOUNTS_FILE_PATH),
                                AccountsResponse.class));

        when(client.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_CREDIT_CARDS_FILE_PATH),
                                CreditCardsResponse.class));
        when(client.fetchCreditCardDetails(contains(NordeaTestData.CREDIT_CARD_ID)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_CREDIT_CARDS_DETAILS_FILE_PATH),
                                CreditCardDetailsResponse.class));
        when(client.fetchCreditCardTransactions(NordeaTestData.CREDIT_CARD_ID, 1))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_CREDIT_CARD_TRANSACTIONS_FILE_PATH),
                                CreditCardTransactionsResponse.class));
        when(client.fetchCreditCardTransactions(NordeaTestData.CREDIT_CARD_ID, 2))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(
                                        NordeaTestData
                                                .FETCH_CREDIT_TRANSACTIONS_CONTINUATION_FILE_PATH),
                                CreditCardTransactionsResponse.class));
        when(client.fetchInvestments())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_INVESTMENT_ACCOUNTS_FILE_PATH),
                                CustodyAccountsResponse.class));

        mockTransactionsForCreditCard(
                client,
                NordeaTestData.CREDIT_CARD_ID_WITH_TRANSACTIONS_WITHOUT_DATE,
                NordeaTestData.CREDIT_CARD_TRANSACTIONS_WITHOUT_DATE);

        return client;
    }

    private static void mockTransactionsForCreditCard(
            NordeaDkApiClient client, String creditCardId, String filePath) {
        when(client.fetchCreditCardTransactions(creditCardId, 1))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), CreditCardTransactionsResponse.class));
        // TODO nasty part should be removed with https://tinkab.atlassian.net/browse/ITE-1445
        when(client.fetchCreditCardTransactions(creditCardId, 2))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(
                                        NordeaTestData
                                                .FETCH_CREDIT_TRANSACTIONS_CONTINUATION_FILE_PATH),
                                CreditCardTransactionsResponse.class));
    }
}
