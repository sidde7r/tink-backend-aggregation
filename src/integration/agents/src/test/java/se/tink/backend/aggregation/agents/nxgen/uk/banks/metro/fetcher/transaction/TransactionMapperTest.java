package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class TransactionMapperTest {
    private static final String CURRENCY_CODE = "GBP";

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    public void shouldMapMetroTransactionToTinkTransactionObjectWithSingleDescription() {
        // given
        TransactionEntity transaction =
                TransactionFixtures.TRANSACTION_WITH_ONE_PART_OF_DESCRIPTION.toObject();

        // when
        AggregationTransaction result = mapper.map(transaction, CURRENCY_CODE);

        // then
        assertThat(result.getProviderMarket()).isEqualTo("UK");
        assertThat(result.getDescription()).isEqualTo("Credit Interest");
        assertThat(result.getAmount().getExactValue()).isEqualTo(BigDecimal.valueOf(0.01));
        assertThat(result.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);
        assertThat(
                        result.getExternalSystemIds()
                                .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID))
                .isEqualTo("190276713404399.111111");
    }

    @Test
    public void shouldMapMetroTransactionToTinkTransactionObjectWithFullDescription() {
        // given
        TransactionEntity transaction =
                TransactionFixtures.TRANSACTION_WITH_FULL_DESCRIPTION.toObject();

        // when
        AggregationTransaction result = mapper.map(transaction, CURRENCY_CODE);

        // then
        assertThat(result.getProviderMarket()).isEqualTo("UK");
        assertThat(result.getDescription()).isEqualTo("Credit Interest - Transaction");
        assertThat(result.getAmount().getExactValue()).isEqualTo(BigDecimal.valueOf(0.01));
        assertThat(result.getAmount().getCurrencyCode()).isEqualTo(CURRENCY_CODE);
        assertThat(
                        result.getExternalSystemIds()
                                .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID))
                .isEqualTo("190276713404399.222222");
    }
}
