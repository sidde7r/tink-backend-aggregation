package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankCardTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/resources";
    private static final String TEST_CARD_ID = "test_card_id";
    private static final String NEXT_PAGE_KEY = "test_next_key";

    private SparebankApiClient mockApiClient;
    private SparebankCardTransactionMapper mockCardTransactionMapper;

    private SparebankCardTransactionFetcher cardTransactionFetcher;

    private CreditCardAccount testCardAccount;

    @Before
    public void setup() {

        mockApiClient = mock(SparebankApiClient.class);
        mockCardTransactionMapper = mock(SparebankCardTransactionMapper.class);

        cardTransactionFetcher =
                new SparebankCardTransactionFetcher(mockApiClient, mockCardTransactionMapper);

        testCardAccount = mock(CreditCardAccount.class);

        given(testCardAccount.getApiIdentifier()).willReturn(TEST_CARD_ID);
    }

    @Test
    public void shouldFetchFirstPageIfKeyNull() {
        // given
        CardTransactionResponse transactions = getTransactionsWithNext();
        given(mockApiClient.fetchCardTransactions(TEST_CARD_ID)).willReturn(transactions);

        // when
        TransactionKeyPaginatorResponse<String> transactionsPaginator =
                cardTransactionFetcher.getTransactionsFor(testCardAccount, null);

        // then
        assertThat(transactionsPaginator.nextKey()).isEqualTo(NEXT_PAGE_KEY);
        verify(mockApiClient).fetchCardTransactions(testCardAccount.getApiIdentifier());
        verify(mockCardTransactionMapper).toTinkTransactions(transactions.getCardTransactions());
    }

    @Test
    public void shouldFetchNextPageIfKeyNotNull() {
        // given
        CardTransactionResponse transactionsLastPage = getTransactionsLastPage();
        given(mockApiClient.fetchNextCardTransactions(NEXT_PAGE_KEY))
                .willReturn(transactionsLastPage);
        // when
        TransactionKeyPaginatorResponse<String> transactionsPaginator =
                cardTransactionFetcher.getTransactionsFor(testCardAccount, NEXT_PAGE_KEY);

        // then
        assertThat(transactionsPaginator.nextKey()).isNull();
        verify(mockApiClient).fetchNextCardTransactions(NEXT_PAGE_KEY);
        verify(mockCardTransactionMapper)
                .toTinkTransactions(transactionsLastPage.getCardTransactions());
    }

    private CardTransactionResponse getTransactionsLastPage() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "cardTransactions.json").toFile(),
                CardTransactionResponse.class);
    }

    private CardTransactionResponse getTransactionsWithNext() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "cardTransactionsWithNext.json").toFile(),
                CardTransactionResponse.class);
    }
}
