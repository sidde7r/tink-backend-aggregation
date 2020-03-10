package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.rpc.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaDkTestUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    static NordeaDkApiClient mockApiClient() {
        NordeaDkApiClient client = mock(NordeaDkApiClient.class);
        try {
            when(client.getAccounts())
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_ACCOUNTS_RESULT, AccountsResponse.class));
            when(client.getAccountTransactions(
                            contains(NordeaTestData.ACCOUNT_1_API_ID),
                            contains("UNGOPS"),
                            isNull()))
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_ACCOUNT_1_TRANSACTIONS_RESULT,
                                    TransactionsResponse.class));
            when(client.getAccountTransactions(
                            contains(NordeaTestData.ACCOUNT_1_API_ID),
                            contains("UNGOPS"),
                            contains(NordeaTestData.TRANSACTIONS_CONTINUATION_KEY)))
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_ACCOUNT_1_TRANSACTIONS_CONTINUATION,
                                    TransactionsResponse.class));
            when(client.fetchCreditCards())
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_CREDIT_CARDS, CreditCardsResponse.class));
            when(client.fetchCreditCardDetails(contains(NordeaTestData.CREDIT_CARD_ID)))
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_CREDIT_CARD_DETAILS,
                                    CreditCardDetailsResponse.class));
            when(client.fetchCreditCardTransactions(NordeaTestData.CREDIT_CARD_ID, 1))
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_CREDIT_CARD_TRANSACTIONS_PAGE1,
                                    CreditCardTransactionsResponse.class));
            when(client.fetchCreditCardTransactions(NordeaTestData.CREDIT_CARD_ID, 2))
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_CREDIT_CARD_TRANSACTIONS_PAGE2,
                                    CreditCardTransactionsResponse.class));
            when(client.fetchInvestments())
                    .thenReturn(
                            mapper.readValue(
                                    NordeaTestData.FETCH_INVESTMENT_ACCOUNTS,
                                    CustodyAccountsResponse.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    static SessionStorage mockSessionStorage() {
        return mock(SessionStorage.class);
    }
}
