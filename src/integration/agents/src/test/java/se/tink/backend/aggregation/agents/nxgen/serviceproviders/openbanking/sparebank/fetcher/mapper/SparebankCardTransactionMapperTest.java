package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankCardTransactionMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/resources";

    private SparebankCardTransactionMapper transactionMapper;

    private CardTransactionsEntity testTransactionEntity;

    @Before
    public void setup() {
        transactionMapper = new SparebankCardTransactionMapper();

        testTransactionEntity = getCardTransactionsEntity();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenFailedToParse() {
        // when
        Optional<Transaction> maybeTransaction = transactionMapper.toTinkTransaction(null, false);

        // then
        assertThat(maybeTransaction.isPresent()).isFalse();
    }

    @Test
    public void shouldMapBothPendingAndBookedTransactions() {
        // when
        Collection<Transaction> transactions =
                transactionMapper.toTinkTransactions(testTransactionEntity);

        // then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.stream().filter(Transaction::isPending).count()).isEqualTo(1);
    }

    @Test
    public void shouldReturnOnlyProperlyMappedTransactions() {
        // given
        testTransactionEntity.getBooked().set(0, null);

        // when
        Collection<Transaction> transactions =
                transactionMapper.toTinkTransactions(testTransactionEntity);

        // then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.stream().filter(Transaction::isPending).count()).isEqualTo(1);
    }

    @Test
    public void shouldMapBookedTransactionProperly() {
        // given
        CardTransactionEntity cardTransactionEntity = testTransactionEntity.getBooked().get(0);

        // when
        Optional<Transaction> maybeTransaction =
                transactionMapper.toTinkTransaction(cardTransactionEntity, false);

        // then
        assertThat(maybeTransaction.isPresent()).isTrue();

        Transaction transaction = maybeTransaction.get();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(200.26, "NOK"));
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2018-09-01");
        assertThat(transaction.getDescription()).isEqualTo("Details 1234 of booked");
    }

    @Test
    public void shouldMapPendingTransactionProperly() {
        // given
        CardTransactionEntity cardTransactionEntity = testTransactionEntity.getPending().get(0);

        // when
        Optional<Transaction> maybeTransaction =
                transactionMapper.toTinkTransaction(cardTransactionEntity, true);

        // then
        assertThat(maybeTransaction.isPresent()).isTrue();

        Transaction transaction = maybeTransaction.get();
        assertThat(transaction.isPending()).isTrue();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(100.26, "NOK"));
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2018-09-01");
        assertThat(transaction.getDescription()).isEqualTo("Details 1234 of pending");
    }

    private CardTransactionsEntity getCardTransactionsEntity() {
        return SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "cardTransactions.json").toFile(),
                        CardTransactionResponse.class)
                .getCardTransactions();
    }
}
