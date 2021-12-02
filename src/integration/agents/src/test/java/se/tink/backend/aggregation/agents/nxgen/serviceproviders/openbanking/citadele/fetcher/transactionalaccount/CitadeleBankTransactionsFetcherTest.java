package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.TransactionsBaseResponseEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
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
        assertEquals("ALONA OSTAPENKO", result.getDescription());
        assertEquals(
                "d39adfd5-ccce-4b0c-91d9-4e4b9287c041",
                result.getExternalSystemIds()
                        .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID));
        assertEquals("EUR", result.getAmount().getCurrencyCode());
        assertEquals("INWARD TRANSFER", result.getProprietaryFinancialInstitutionType());
        assertEquals("LV", result.getProviderMarket());
        assertEquals("Fri Jan 04 11:00:00 UTC 2019", result.getDate().toString());
    }
}
