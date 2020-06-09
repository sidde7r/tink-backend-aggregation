package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
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

        return new PaginationKey(formId, source, getViewState(), newConsecutiveEmptyReplies);
    }

    private Transaction rowToTransaction(Node row) {
        // transaction rows have 5 cells: date (hidden accessibility cell), (fecha valor),
        // description, amount, account balance
        final Double numberOfColumns = evaluateXPath(row, "count(td)", Double.class);
        if (null == numberOfColumns || numberOfColumns.intValue() != 5) {
            throw new IllegalStateException(
                    "Transaction row should have 5 columns, but has " + numberOfColumns);
        }

        Transaction.Builder builder = Transaction.builder();

        final String thisRowsDate =
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

        final String date =
                Objects.firstNonNull(
                        Strings.emptyToNull(thisRowsDate), Strings.emptyToNull(lastDate));
        final String description = evaluateXPath(row, "td[3]/span[1]", String.class).trim();
        final String amount = evaluateXPath(row, "td[4]", String.class);

        return builder.setDate(parseTransactionDate(date))
                .setDescription(description)
                .setAmount(parseAmount(amount))
                .build();
    }

    private boolean hasNoTransactionsIndicator() {
        return evaluateXPath(transactions, "//tr[contains(@class,'sinMov')]", Node.class) != null;
    }

    private NodeList getTransactionRows() {
        if (null == transactionRows) {
            transactionRows =
                    evaluateXPath(
                            transactions,
                            "//table//tr[contains(@class,'movilDetalleMovimiento')]",
                            NodeList.class);
        }
        return transactionRows;
    }

    public Collection<Transaction> toTinkTransactions() {
        final NodeList transactionRows = getTransactionRows();
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionRows.getLength(); i++) {
            transactions.add(rowToTransaction(transactionRows.item(i)));
        }
        Preconditions.checkState(
                transactions.size() > 0 || hasNoTransactionsIndicator(),
                "No transactions and no empty list indicator. HTML changed?");
        return transactions;
    }
}
