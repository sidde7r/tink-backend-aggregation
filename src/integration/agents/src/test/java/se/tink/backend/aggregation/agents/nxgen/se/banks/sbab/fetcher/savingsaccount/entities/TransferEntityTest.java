package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransferEntityTest {

    private static final String ENTITY_TYPE_TO_REPLACE = "--TRANSFER-ENTITY-TYPE--";
    private static final String ENTITY_STATEMENT = "statement description";
    private static final String CURRENCY_CODE = "SEK";
    private static final String TRANSFER_ENTITY_JSON_RAW =
            "{"
                    + "    \"type\": \""
                    + ENTITY_TYPE_TO_REPLACE
                    + "\","
                    + "    \"statement\": \""
                    + ENTITY_STATEMENT
                    + "\","
                    + "    \"amount\": 123456789,"
                    + "    \"accounting_date\": \"2020-04-21\""
                    + "}";

    @Test
    public void toTinkTransactionWithDepositType() {
        // given
        String depositJson = TRANSFER_ENTITY_JSON_RAW.replace(ENTITY_TYPE_TO_REPLACE, "deposit");
        // and
        TransferEntity transferEntity =
                SerializationUtils.deserializeFromString(depositJson, TransferEntity.class);

        // when
        Transaction result = transferEntity.toTinkTransaction();

        // then
        assertThat(result.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(123456789d, CURRENCY_CODE));
        assertThat(result.getDescription()).isEqualTo(ENTITY_STATEMENT);
    }

    @Test
    public void toTinkTransactionWithInterestRateType() {
        // given
        String depositJson =
                TRANSFER_ENTITY_JSON_RAW.replace(ENTITY_TYPE_TO_REPLACE, "interest_rate");
        // and
        TransferEntity transferEntity =
                SerializationUtils.deserializeFromString(depositJson, TransferEntity.class);

        // when
        Transaction result = transferEntity.toTinkTransaction();

        // then
        assertThat(result.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(123456789d, CURRENCY_CODE));
        assertThat(result.getDescription()).isEqualTo(ENTITY_STATEMENT);
    }

    @Test
    public void toTinkTransactionWithWithdrawalType() {
        // given
        String depositJson = TRANSFER_ENTITY_JSON_RAW.replace(ENTITY_TYPE_TO_REPLACE, "withdrawal");
        // and
        TransferEntity transferEntity =
                SerializationUtils.deserializeFromString(depositJson, TransferEntity.class);

        // when
        Transaction result = transferEntity.toTinkTransaction();

        // then
        assertThat(result.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(-123456789d, CURRENCY_CODE));
        assertThat(result.getDescription()).isEqualTo(ENTITY_STATEMENT);
    }

    @Test
    public void toTinkTransactionWithOtherType() {
        // given
        String depositJson = TRANSFER_ENTITY_JSON_RAW.replace(ENTITY_TYPE_TO_REPLACE, "other");
        // and
        TransferEntity transferEntity =
                SerializationUtils.deserializeFromString(depositJson, TransferEntity.class);

        // when
        Transaction result = transferEntity.toTinkTransaction();

        // then
        assertThat(result.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(-123456789d, CURRENCY_CODE));
        assertThat(result.getDescription()).isEqualTo(ENTITY_STATEMENT);
    }
}
