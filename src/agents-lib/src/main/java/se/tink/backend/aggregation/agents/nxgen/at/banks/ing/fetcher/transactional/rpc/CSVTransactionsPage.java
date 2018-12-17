package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional.rpc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CSVTransactionsPage implements PaginatorResponse {
    private final String csvBody;
    private final boolean keepFetching;

    public CSVTransactionsPage(final String csvBody, final boolean keepFetching) {
        this.csvBody = csvBody;
        this.keepFetching = keepFetching;
    }

    private static Transaction recordToTransaction(final CSVRecord record) {
        final String iban = record.get(0);
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
            throw new IllegalStateException();
        }
        final double amountValue =
                Double.parseDouble(outgoingAmount.replace(".", "").replace(",", "."));
        final Amount amount = new Amount(currency.trim(), amountValue);

        return Transaction.builder().setDate(date).setAmount(amount).setDescription(text).build();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        final CSVParser parser;
        try {
            parser = CSVParser.parse(csvBody, CSVFormat.DEFAULT.withHeader().withDelimiter(';'));
        } catch (IOException e) {
            throw new IllegalStateException();
        }

        try {
            return parser.getRecords()
                    .stream()
                    .map(CSVTransactionsPage::recordToTransaction)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(keepFetching);
    }
}
