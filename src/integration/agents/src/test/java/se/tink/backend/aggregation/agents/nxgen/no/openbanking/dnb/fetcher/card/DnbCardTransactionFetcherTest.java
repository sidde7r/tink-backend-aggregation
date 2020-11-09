package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbCardTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_CARD_ID = "test_card_id";

    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;
    private DnbTransactionMapper mockTransactionMapper;

    private DnbCardTransactionFetcher cardTransactionFetcher;

    private CreditCardAccount testCardAccount;

    @Before
    public void setup() {
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        mockTransactionMapper = mock(DnbTransactionMapper.class);

        cardTransactionFetcher =
                new DnbCardTransactionFetcher(mockStorage, mockApiClient, mockTransactionMapper);

        testCardAccount = mock(CreditCardAccount.class);
    }

    @Test
    public void shouldAlwaysReturnPageWithNoNextKeyAndExpectedNumberOfTransactions() {
        // given
        given(testCardAccount.getApiIdentifier()).willReturn(TEST_CARD_ID);
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchCardTransactions(TEST_CONSENT_ID, TEST_CARD_ID))
                .willReturn(getCardTransactionsResponse());

        Transaction dummyTransaction = mock(Transaction.class);
        Collection<Transaction> testMappedTransactions =
                ImmutableList.of(dummyTransaction, dummyTransaction);
        given(mockTransactionMapper.toTinkTransactions(any(TransactionEntity.class)))
                .willReturn(testMappedTransactions);

        // when
        TransactionKeyPaginatorResponse<String> pageOfTransactions =
                cardTransactionFetcher.getTransactionsFor(testCardAccount, null);

        // then
        assertThat(pageOfTransactions.nextKey()).isNull();
        assertThat(pageOfTransactions.getTinkTransactions()).hasSameSizeAs(testMappedTransactions);

        verify(testCardAccount).getApiIdentifier();
        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchCardTransactions(TEST_CONSENT_ID, TEST_CARD_ID);
        verify(mockTransactionMapper).toTinkTransactions(any(TransactionEntity.class));
        verifyNoMoreInteractionsOnAllMocks();
    }

    private void verifyNoMoreInteractionsOnAllMocks() {
        verifyNoMoreInteractions(
                mockStorage, mockApiClient, mockTransactionMapper, testCardAccount);
    }

    private CardTransactionResponse getCardTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "cardTransactions.json").toFile(),
                CardTransactionResponse.class);
    }
}
