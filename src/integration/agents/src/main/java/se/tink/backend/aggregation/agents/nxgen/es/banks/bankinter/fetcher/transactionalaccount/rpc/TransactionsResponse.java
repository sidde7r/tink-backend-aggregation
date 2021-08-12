package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionsResponse extends JsfUpdateResponse {
    private final Document navigation;
    private final Document transactions;
    private NodeList transactionRows;
    private static final Pattern JSF_SOURCE_PATTERN = Pattern.compile("\\bsource:'([^']+)'");
    private static final Pattern JSF_FORM_ID_PATTERN = Pattern.compile("\\bformId:'([^']+)'");
    private static final Logger LOG = LoggerFactory.getLogger(TransactionsResponse.class);

    public TransactionsResponse(String body) {
        super(body);
        this.navigation = getUpdateDocument(JsfPart.TRANSACTIONS_NAVIGATION);
        this.transactions = getUpdateDocument(JsfPart.TRANSACTIONS);
    }

    public PaginationKey getNextKey(long consecutiveEmptyReplies) {
        // first script contains link to previous month
        // there's always a link, even if there are no more transactions
        final String script = evaluateXPath(navigation, "//script[1]/comment()", String.class);
        final String formId = getPaginationFormId(script);
        final String source = getPreviousMonthJsfSource(script);

        final long newConsecutiveEmptyReplies;
        if (getTransactionRows().getLength() == 0) {
            newConsecutiveEmptyReplies = consecutiveEmptyReplies + 1;
        } else {
            newConsecutiveEmptyReplies = 0;
        }

        return new PaginationKey(
                formId,
                source,
                getViewState(),
                newConsecutiveEmptyReplies,
                getFirstTransactionDate());
    }

    public Date getFirstTransactionDate() {
        if (getTransactionRows().getLength() > 0) {
            final Transaction transaction = rowToTransaction(getTransactionRows().item(0));
            return transaction.getDate();
        }
        return null;
    }

    public List<Transaction> toTinkTransactions() {
        final NodeList rows = getTransactionRows();
        ArrayList<Transaction> transactionList = new ArrayList<>();
        for (int i = 0; i < rows.getLength(); i++) {
            transactionList.add(rowToTransaction(rows.item(i)));
        }
        Preconditions.checkState(
                transactionList.size() > 0 || hasNoTransactionsIndicator(),
                "No transactions and no empty list indicator. HTML changed?");
        return transactionList;
    }

    private String getPaginationFormId(String script) {
        final Matcher matcher = JSF_FORM_ID_PATTERN.matcher(script);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Did not find form ID for previous transactions page.");
        }
    }

    private String getPreviousMonthJsfSource(String script) {
        final Matcher matcher = JSF_SOURCE_PATTERN.matcher(script);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Did not find source for previous transactions page.");
        }
    }

    private Transaction rowToTransaction(Node row) {
        // transaction rows have 5 cells:
        // accounting date, value date, description, amount, account balance
        if (!has5columns(row)) {
            throw new IllegalStateException("Transaction row should have 5 columns.");
        }

        LocalDate date = getAccountingDate(row);
        LocalDate valueDate = getValueDate(row);
        final String description = getDescription(row);
        final String amount = getAmount(row);

        return Transaction.builder()
                .setTransactionDates(
                        TransactionDates.builder()
                                .setValueDate(new AvailableDateInformation(valueDate))
                                .setBookingDate(new AvailableDateInformation(date))
                                .build())
                .setDescription(description)
                .setAmount(parseAmount(amount))
                .setDate(date)
                .build();
    }

    private boolean has5columns(Node row) {
        return getNumberOfColumns(row).filter(num -> num.intValue() == 5).isPresent();
    }

    private Optional<Double> getNumberOfColumns(Node row) {
        return Optional.ofNullable(evaluateXPath(row, "count(td)", Double.class));
    }

    private LocalDate getAccountingDate(Node row) {
        final String thisRowsAccountingDate =
                evaluateXPath(
                                row,
                                "th[starts-with(@id,'FechaContable') and not(contains(@class,'empty'))]",
                                String.class)
                        .trim();
        final String lastDate =
                evaluateXPath(
                                row,
                                "preceding::th[starts-with(@id,'FechaContable') and not(contains(@class,'empty'))][1]",
                                String.class)
                        .trim();
        final String dateStr =
                Objects.firstNonNull(
                        Strings.emptyToNull(thisRowsAccountingDate), Strings.emptyToNull(lastDate));
        return parseTransactionDate(dateStr);
    }

    private LocalDate getValueDate(Node row) {
        final String valueDateStr =
                evaluateXPath(
                                row,
                                "td[starts-with(@id,'FechaValor') and not(contains(@class,'empty'))]",
                                String.class)
                        .trim();
        return parseTransactionDate(valueDateStr);
    }

    private String getDescription(Node row) {
        return evaluateXPath(row, "td[3]/span[1]", String.class).trim();
    }

    private String getAmount(Node row) {
        return evaluateXPath(row, "td[4]", String.class);
    }

    private boolean hasNoTransactionsIndicator() {
        return evaluateXPath(transactions, "//tr[contains(@class,'sinMov')]", Node.class) != null;
    }

    private NodeList getTransactionRows() {
        if (null == transactionRows) {
            transactionRows =
                    evaluateXPath(
                            transactions,
                            "//table//tr[contains(@class,'movilDetalleMovimiento') and contains(@onclick,\"clickDetalle('true\")]",
                            NodeList.class);
        }
        return transactionRows;
    }
}
