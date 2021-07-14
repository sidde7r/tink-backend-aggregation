package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.TransactionsBaseResponseEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CitadeleBankTransactionsFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/citadele/fetcher/transactionalaccount/resources";
    final String market = "LV";

    CitadeleTransactionFetcher transactionFetcher = new CitadeleTransactionFetcher(null, market);

    @Test
    public void shouldMapToTinkTransactions() {

        // given
        TransactionsBaseResponseEntity listTransactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        TransactionsBaseResponseEntity.class);

        // when
        AggregationTransaction result =
                transactionFetcher.getTinkTransactions(market, listTransactionsResponse).get(0);

        // then
        assertThat(result)
                .isEqualToComparingFieldByFieldRecursively(getExpectedTransactionResponse().get(0));
    }

    private List<AggregationTransaction> getExpectedTransactionResponse() {
        ExactCurrencyAmount exactCurrencyAmount =
                new ExactCurrencyAmount(BigDecimal.valueOf(500.00), "EUR");
        return Collections.singletonList(
                Transaction.builder()
                        .setAmount(exactCurrencyAmount)
                        .setDescription(
                                "Payment to Own Account ALONA OSTAPENKO PARXLV22 LV35PARX0016354460002")
                        .setPending(false)
                        .setDate(Date.valueOf("2019-01-04"))
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                "d39adfd5-ccce-4b0c-91d9-4e4b9287c041")
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType("INWARD TRANSFER")
                        .setProviderMarket(market)
                        .build());
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(null));

        if (Objects.nonNull(Date.valueOf("2019-01-04"))) {
            builder.setBookingDate(
                    new AvailableDateInformation().setDate(LocalDate.of(2019, 1, 4)));
        }

        return builder.build();
    }
}
