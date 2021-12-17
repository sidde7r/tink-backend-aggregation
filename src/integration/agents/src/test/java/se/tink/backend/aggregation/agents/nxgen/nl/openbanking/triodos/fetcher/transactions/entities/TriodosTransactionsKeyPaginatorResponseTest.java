package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.transactions.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class TriodosTransactionsKeyPaginatorResponseTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/fetcher/transactions/entities/resources/";

    @Test
    @Parameters()
    public void shouldCorrectlyMapCounterpartyAccountInTinkTransaction(
            String transactionsFile, String accountNumber) {
        TriodosTransactionsKeyPaginatorResponse triodosTransactionsKeyPaginatorResponse =
                deserializeFromFile(transactionsFile);

        // when
        Collection<? extends Transaction> tinkTransactions =
                triodosTransactionsKeyPaginatorResponse.getTinkTransactions();

        // then
        assertThat(tinkTransactions)
                .isNotEmpty()
                .allMatch(
                        transaction ->
                                Objects.equals(
                                        transaction.getPayload().get(TRANSFER_ACCOUNT_EXTERNAL),
                                        accountNumber));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldCorrectlyMapCounterpartyAccountInTinkTransaction() {
        return new Object[][] {
            {"triodos-transactions-creditor.json", "NL30RABO0000009999"},
            {"triodos-transactions-debtor.json", "NL30RABO0000008888"},
            {"triodos-transactions-name-without-iban.json", null},
        };
    }

    @Test
    public void shouldNotMapCreditorAccountInTinkTransactionWhenNoIban() {
        TriodosTransactionsKeyPaginatorResponse triodosTransactionsKeyPaginatorResponse =
                deserializeFromFile("triodos-transactions-creditor-no-name-no-iban.json");

        // when
        Collection<? extends Transaction> tinkTransactions =
                triodosTransactionsKeyPaginatorResponse.getTinkTransactions();

        // then
        assertThat(tinkTransactions)
                .isNotEmpty()
                .noneMatch(
                        transaction ->
                                transaction.getPayload().containsKey(TRANSFER_ACCOUNT_EXTERNAL));
    }

    private static TriodosTransactionsKeyPaginatorResponse deserializeFromFile(String fileName) {
        return SerializationUtils.deserializeFromString(
                Paths.get(RESOURCE_PATH, fileName).toFile(),
                TriodosTransactionsKeyPaginatorResponse.class);
    }
}
