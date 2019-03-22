package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public class IngAtCreditCardTransactionParser {
    private final Document doc;

    public IngAtCreditCardTransactionParser(Document doc) {
        this.doc = doc;
    }

    public IngAtCreditCardTransactionParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDoc() {
        return doc;
    }

    public Collection<Transaction> getTransactions() {
        Collection<Transaction> result = new ArrayList<>();
        final Elements transactionTable = doc.select("table.transactions-table");
        if (Objects.isNull(transactionTable)) {
            return result;
        }
        final Elements rows = transactionTable.select("tr.transactions-table__entry-table__row");
        if (Objects.isNull(rows)) {
            return result;
        }
        for (Element row : rows) {
            toTransaction(row).ifPresent(result::add);
        }
        return result;
    }

    private Optional<Transaction> toTransaction(Element row) {
        Date transactionDate = getDateFromRow(row);
        String transactionDetails = getDetailsFromRow(row);
        Amount amount = getAmountFromRow(row);
        if (transactionDate == null || amount == null || transactionDetails.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                Transaction.builder()
                        .setDescription(transactionDetails)
                        .setDate(transactionDate)
                        .setAmount(amount)
                        .build());
    }

    private Date getDateFromRow(final Element row) {
        Element dateElement = row.select("td.transactions-table__date-col").first();
        if (Objects.isNull(dateElement) || dateElement.text().isEmpty()) {
            return null;
        }
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return formatter.parse(dateElement.text());
        } catch (ParseException e) {
            // TODO Log
        }
        return null;
    }

    private String getDetailsFromRow(final Element row) {
        Element detailsElement = row.select("td.transactions-table__detail-col").first();
        if (Objects.isNull(detailsElement)) {
            return "";
        }
        Element transactionName = detailsElement.select("p.transaction-name__text").first();
        if (Objects.isNull(transactionName) || transactionName.text().isEmpty()) {
            return "";
        }
        return transactionName.text();
    }

    private Amount getAmountFromRow(final Element row) {
        Element amountElement = row.select("td.transactions-table__amount-col").first();
        if (Objects.isNull(amountElement) || amountElement.text().isEmpty()) {
            return null;
        }
        return IngAtAmmountParser.toAmount(amountElement.text());
    }
}
