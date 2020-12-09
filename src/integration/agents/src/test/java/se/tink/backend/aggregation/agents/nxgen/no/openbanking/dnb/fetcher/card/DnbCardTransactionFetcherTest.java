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
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbCardTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_CARD_ID = "test_card_id";
    private static final Date TEST_DATE_FROM = new Date();
    private static final Date TEST_DATE_TO = new Date();

    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;
    private DnbTransactionMapper mockTransactionMapper;

    private DnbCardTransactionFetcher cardTransactionFetcher;

    private CreditCardAccount testCardAccount;
    private Collection<Transaction> testMappedTransactions;

    @Before
    public void setup() {
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        mockTransactionMapper = mock(DnbTransactionMapper.class);

        testCardAccount = mock(CreditCardAccount.class);

        given(testCardAccount.getApiIdentifier()).willReturn(TEST_CARD_ID);
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(
                        mockApiClient.fetchCardTransactions(
                                TEST_CONSENT_ID, TEST_CARD_ID, TEST_DATE_FROM, TEST_DATE_TO))
                .willReturn(getCardTransactionsResponse());

        Transaction dummyTransaction = mock(Transaction.class);
        testMappedTransactions = ImmutableList.of(dummyTransaction, dummyTransaction);
        given(mockTransactionMapper.toTinkTransactions(any(TransactionEntity.class)))
                .willReturn(testMappedTransactions);
    }

    @Test
    public void
            shouldAlwaysReturnPageWithCanFetchMoreAndExpectedNumberOfTransactionsWhenManualRefresh() {
        // given
        cardTransactionFetcher =
                new DnbCardTransactionFetcher(
                        mockStorage, mockApiClient, mockTransactionMapper, true);

        // when
        PaginatorResponse pageOfTransactions =
                cardTransactionFetcher.getTransactionsFor(
                        testCardAccount, TEST_DATE_FROM, TEST_DATE_TO);

        // then
        assertThat(pageOfTransactions.canFetchMore()).isEqualTo(Optional.of(true));
        assertThat(pageOfTransactions.getTinkTransactions()).hasSameSizeAs(testMappedTransactions);

        verify(testCardAccount).getApiIdentifier();
        verify(mockStorage).getConsentId();
        verify(mockApiClient)
                .fetchCardTransactions(TEST_CONSENT_ID, TEST_CARD_ID, TEST_DATE_FROM, TEST_DATE_TO);
        verify(mockTransactionMapper).toTinkTransactions(any(TransactionEntity.class));
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldNeverAllowForMorePagesWhenAutoRefresh() {
        // given
        cardTransactionFetcher =
                new DnbCardTransactionFetcher(
                        mockStorage, mockApiClient, mockTransactionMapper, false);

        // when
        PaginatorResponse pageOfTransactions =
                cardTransactionFetcher.getTransactionsFor(
                        testCardAccount, TEST_DATE_FROM, TEST_DATE_TO);

        // then
        assertThat(pageOfTransactions.canFetchMore()).isEqualTo(Optional.of(false));
        assertThat(pageOfTransactions.getTinkTransactions()).hasSameSizeAs(testMappedTransactions);

        verify(testCardAccount).getApiIdentifier();
        verify(mockStorage).getConsentId();
        verify(mockApiClient)
                .fetchCardTransactions(TEST_CONSENT_ID, TEST_CARD_ID, TEST_DATE_FROM, TEST_DATE_TO);
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
