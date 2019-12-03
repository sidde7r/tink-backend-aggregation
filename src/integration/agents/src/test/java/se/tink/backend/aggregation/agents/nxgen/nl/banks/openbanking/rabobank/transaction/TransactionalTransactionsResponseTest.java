package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.transaction;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionalTransactionsResponseTest {

    @Test
    public void shouldConvertRabobankTransactionsToTinkTransactionsModel() {
        final File trxFile =
                new File(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources/transactions.json");

        TransactionalTransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        trxFile, TransactionalTransactionsResponse.class);

        Transaction transaction =
                Transaction.builder()
                        .setAmount(
                                response.getTransactions()
                                        .getBooked()
                                        .get()
                                        .get(0)
                                        .getTransactionAmount())
                        .setDate(
                                response.getTransactions().getBooked().get().get(0).getBookedDate())
                        .setDescription(
                                response.getTransactions()
                                        .getBooked()
                                        .get()
                                        .get(0)
                                        .getCreditorName())
                        .setPending(false)
                        .setPayload(
                                TransactionPayloadTypes.DETAILS,
                                response.getTransactions()
                                        .getBooked()
                                        .get()
                                        .get(0)
                                        .getRaboDetailedTransactionType())
                        .build();

        Assert.assertEquals(transaction.getPayload().get(TransactionPayloadTypes.DETAILS), "1");
    }
}
