package se.tink.backend.aggregation.nxgen.core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AggregationTransactionTest {

    @Test
    public void getPayloadTest() {
        Map<TransactionPayloadTypes, String> payloads = new HashMap<>();
        payloads.put(TransactionPayloadTypes.DETAILS, "details");
        Transaction transaction =
                new Transaction(
                        new ExactCurrencyAmount(BigDecimal.valueOf(1.00), "EUR"),
                        new Date(),
                        "Description",
                        false,
                        "rawDetails",
                        "externalId",
                        TransactionTypes.PAYMENT,
                        null,
                        payloads,
                        new HashMap<>(),
                        false,
                        new ArrayList<>(),
                        "fakeProprietaryFinancialInstitutionType",
                        "fakeMerchantName",
                        "fakeMerchantCategoryCode",
                        "fakeTransactionReference",
                        "fakeProviderMarket");

        assertEquals("details", transaction.getPayload().get(TransactionPayloadTypes.DETAILS));
    }

    @Test
    public void shouldSetFieldMapperMigrationsInPayloadWhenTranslatingToIternalModel() {
        Transaction transaction =
                transactionBuilder()
                        .setFieldsMigrations(
                                FieldsMigrations.builder()
                                        .migration(DateFieldMigration.version1(new Date(1500000L)))
                                        .build())
                        .build();

        se.tink.backend.aggregation.agents.models.Transaction systemTransaction =
                transaction.toSystemTransaction(false);

        assertEquals(
                "[{\"v1\":{\"originalDate\":1500000}}]",
                systemTransaction
                        .getPayload()
                        .get(TransactionPayloadTypes.FIELD_MAPPER_MIGRATIONS));
    }

    @Test
    public void shouldSkipFieldMapperMigrationsEnumIfMigrationsEmpty() {
        Transaction transaction =
                transactionBuilder()
                        .setFieldsMigrations(FieldsMigrations.builder().build())
                        .build();

        se.tink.backend.aggregation.agents.models.Transaction systemTransaction =
                transaction.toSystemTransaction(false);

        assertEquals(
                null,
                systemTransaction
                        .getPayload()
                        .get(TransactionPayloadTypes.FIELD_MAPPER_MIGRATIONS));
    }

    @Test
    public void shouldSkipFieldMapperMigrationsEnumIfNoMigrations() {
        Transaction transaction = transactionBuilder().build();

        se.tink.backend.aggregation.agents.models.Transaction systemTransaction =
                transaction.toSystemTransaction(false);

        assertEquals(
                null,
                systemTransaction
                        .getPayload()
                        .get(TransactionPayloadTypes.FIELD_MAPPER_MIGRATIONS));
    }

    private Builder transactionBuilder() {
        return Transaction.builder().setAmount(ExactCurrencyAmount.inEUR(1));
    }
}
