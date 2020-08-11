package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.rpc.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaDkTestUtils {

    static NordeaDkApiClient mockApiClient() {
        NordeaDkApiClient client = mock(NordeaDkApiClient.class);

        when(client.getAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_ACCOUNTS_FILE_PATH),
                                AccountsResponse.class));
        when(client.getAccountTransactions(
                        contains(NordeaTestData.ACCOUNT_1_API_ID), contains("UNGOPS"), isNull()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(NordeaTestData.FETCH_ACCOUNT_TRANSACTIONS_FILE_PATH),
                                TransactionsResponse.class));
        when(client.getAccountTransactions(
                        contains(NordeaTestData.ACCOUNT_1_API_ID),
                        contains("UNGOPS"),
                        contains(NordeaTestData.TRANSACTIONS_CONTINUATION_KEY)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(
                                        NordeaTestData
                                                .FETCH_ACCOUNT_TRANSACTIONS_CONTINUATION_FILE_PATH),
                                TransactionsResponse.class));
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
        return client;
    }
}
