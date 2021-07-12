package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LuminorTransactionsFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor/fetcher/transactions/resources";

    @Test
    public void shouldMapToTinkTransaction() {

        // given
        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        TransactionsResponse.class);
        // when
        Transaction result =
                transactionsResponse.getTinkTransactions().stream().findFirst().orElse(null);

        // then
        Assert.assertEquals(
                new ExactCurrencyAmount(BigDecimal.valueOf(152.45), "EUR"),
                Objects.requireNonNull(result).getAmount());
        Assert.assertEquals(("Invoice x29876"), Objects.requireNonNull(result).getDescription());
        Assert.assertEquals(
                ("SCOR"), Objects.requireNonNull(result).getProprietaryFinancialInstitutionType());
        Assert.assertEquals(
                ("RF18539007547034"), Objects.requireNonNull(result).getTransactionReference());
        Assert.assertEquals(("Jonas Jonaitis"), Objects.requireNonNull(result).getMerchantName());
        Assert.assertEquals(1, Objects.requireNonNull(result).getExternalSystemIds().size());
        assertThat(result.getExternalSystemIds())
                .containsKey(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID);
        assertThat(result.getExternalSystemIds()).containsValue("40367613");
        Assert.assertEquals(
                2, Objects.requireNonNull(result).getTransactionDates().getDates().size());
        assertThat(result.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.VALUE_DATE);
    }
}
