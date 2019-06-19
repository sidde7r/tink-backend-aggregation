package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class TransactionsResponse extends JsfUpdateResponse {
    private final Document navigation;
    private final Document transactions;
    private NodeList transactionRows;
    private static final DateTimeFormatter TRANSACTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern JSF_SOURCE_PATTERN = Pattern.compile(".*source:'([^']+)'.*");

    public TransactionsResponse(HttpResponse response) {
        super(response);
        this.navigation = getUpdateDocument(JsfPart.TRANSACTIONS_NAVIGATION);
        this.transactions = getUpdateDocument(JsfPart.TRANSACTIONS);
    }

    private String getPreviousMonthJsfSource() {
        // first script contains link to previous month
        final String script = evaluateXPath(navigation, "//script[1]/comment()", String.class);
        final Matcher matcher = JSF_SOURCE_PATTERN.matcher(script);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public PaginationKey getNextKey(long consecutiveEmptyReplies) {
        final long newConsecutiveEmptyReplies;
        if (getTransactionRows().getLength() == 0) {
            newConsecutiveEmptyReplies = consecutiveEmptyReplies + 1;
        } else {
            newConsecutiveEmptyReplies = 0;
        }
        return new PaginationKey(
                getPreviousMonthJsfSource(), getViewState(), newConsecutiveEmptyReplies);
    }

    private Transaction rowToTransaction(Node row) {
        // transaction rows have 4 cells: date (fecha valor), description, amount, account balance
        final String date = evaluateXPath(row, "td[1]", String.class);
        final String description = evaluateXPath(row, "td[2]", String.class).trim();
        final String amount = evaluateXPath(row, "td[3]", String.class).replaceAll("\\s", "");

        return Transaction.builder()
                .setDate(date, TRANSACTION_DATE_FORMATTER)
                .setDescription(description)
                .setAmount(parseAmount(amount))
                .build();
    }

    private NodeList getTransactionRows() {
        if (null == transactionRows) {
            transactionRows =
                    evaluateXPath(
                            transactions,
                            "//table/tr[contains(@class,'movilDetalleMovimiento')]",
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
        return transactions;
    }
}
