package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional.rpc;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public final class CSVTransactionsPage implements PaginatorResponse {

    private final String csvBody;
    private final boolean keepFetching;

    private static double toValue(final String valueString) {
        return Double.parseDouble(valueString.replace(".", "").replace(",", "."));
    }

    private static Transaction recordToTransaction(final CSVRecord record) {
        final String text = record.get(1);
        final String valueDate = record.get(2);
        final String currency = record.get(3);
        final String outgoingAmount = record.get(4);
        final String incomingAmount = record.get(5);

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        final Date date;
        try {
            date = dateFormatter.parse(valueDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        // One of these should be zero, and the other should be a positive amount
        final double outgoingValue = toValue(outgoingAmount);
        final double incomingValue = toValue(incomingAmount);

        if (Math.min(outgoingValue, incomingValue) != 0.0) {
            log.warn(
                    "Found a transaction where both the outgoing and incoming amounts were nonzero");
        }

        final double amountValue = incomingValue - outgoingValue;
        final ExactCurrencyAmount amount = ExactCurrencyAmount.of(amountValue, currency.trim());

        return Transaction.builder().setDate(date).setAmount(amount).setDescription(text).build();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        final CSVParser parser;
        try {
            parser = CSVParser.parse(csvBody, CSVFormat.DEFAULT.withHeader().withDelimiter(';'));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try {
            return parser.getRecords().stream()
                    .map(CSVTransactionsPage::recordToTransaction)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(keepFetching);
    }
}
