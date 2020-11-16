package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbTransactionMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";

    private DnbTransactionMapper transactionMapper;

    TransactionEntity testTransactionEntity;

    @Before
    public void setup() {
        transactionMapper = new DnbTransactionMapper();

        testTransactionEntity = getTransactionEntity();
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
        assertThat(transactions).hasSize(3);
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
        assertThat(transactions).hasSize(2);
        assertThat(transactions.stream().filter(Transaction::isPending).count()).isEqualTo(1);
    }

    @Test
    public void shouldMapBookedTransactionProperly() {
        // given
        TransactionDetailsEntity transactionDetailsEntity =
                testTransactionEntity.getBooked().get(0);

        // when
        Optional<Transaction> maybeTransaction =
                transactionMapper.toTinkTransaction(transactionDetailsEntity, false);

        // then
        assertThat(maybeTransaction.isPresent()).isTrue();

        Transaction transaction = maybeTransaction.get();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(-10.00, "NOK"));
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2020-11-02");
        assertThat(transaction.getDescription()).isEqualTo("Morsom  SDJKFH  VJJjaskfjjkhasdkjhjk");
    }

    @Test
    public void shouldFallBackOnDifferentFieldForDescriptionIfFirstChoiceMissing() {
        // given
        TransactionDetailsEntity transactionDetailsEntity =
                testTransactionEntity.getBooked().get(1);

        // when
        Optional<Transaction> maybeTransaction =
                transactionMapper.toTinkTransaction(transactionDetailsEntity, false);

        // then
        assertThat(maybeTransaction.isPresent()).isTrue();

        Transaction transaction = maybeTransaction.get();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(500.00, "NOK"));
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2020-11-02");
        assertThat(transaction.getDescription()).isEqualTo("ZXCV Description 2");
    }

    @Test
    public void shouldMapPendingTransactionProperly() {
        // given
        TransactionDetailsEntity transactionDetailsEntity =
                testTransactionEntity.getPending().get(0);

        // when
        Optional<Transaction> maybeTransaction =
                transactionMapper.toTinkTransaction(transactionDetailsEntity, true);

        // then
        assertThat(maybeTransaction.isPresent()).isTrue();

        Transaction transaction = maybeTransaction.get();
        assertThat(transaction.isPending()).isTrue();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(-378.00, "NOK"));
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2020-11-03");
        assertThat(transaction.getDescription()).isEqualTo("VISA ASDKFASF");
    }

    private TransactionEntity getTransactionEntity() {
        return SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions.json").toFile(),
                        TransactionResponse.class)
                .getTransactions();
    }
}
