package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class TransactionsTest {

    /** Deserializes a single transactions JSON response. Should not raise an exception. */
    @Test
    public void testTransactions() {
        final File trxFile =
                new File(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources/transactions.json");

        final TransactionalTransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        trxFile, TransactionalTransactionsResponse.class);

        final List<String> trxs =
                response.getTinkTransactions().stream()
                        .map(AggregationTransaction::getDescription)
                        .collect(Collectors.toList());

        trxs.forEach(System.out::println);
    }

    /**
     * Reads an S3 log file and extracts all transactions JSON responses in that file, assuming
     * every JSON blob is on one line. Should not raise an exception.
     */
    @Test
    public void testTransactionsLog() throws IOException {
        final File logFile =
                new File(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources/trxs.s3");

        final List<TransactionalTransactionsResponse> responses = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                final TransactionalTransactionsResponse response =
                        SerializationUtils.deserializeFromString(
                                line, TransactionalTransactionsResponse.class);
                if (Objects.nonNull(response) && Objects.nonNull(response.getTransactions())) {
                    responses.add(response);
                }
            }
        }

        final List<AggregationTransaction> responses2 =
                responses.stream()
                        .map(TransactionalTransactionsResponse::getTinkTransactions)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        final List<String> descriptions =
                responses2.stream()
                        .map(AggregationTransaction::getDescription)
                        .collect(Collectors.toList());

        descriptions.forEach(System.out::println);
    }
}
