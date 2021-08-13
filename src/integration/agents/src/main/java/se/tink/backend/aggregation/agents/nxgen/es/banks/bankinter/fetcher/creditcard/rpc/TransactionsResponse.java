package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

public class TransactionsResponse extends JsfUpdateResponse {
    private final Document navigation;
    private final Document transactions;
    private static final Pattern JSF_SOURCE_PATTERN = Pattern.compile(".*source:'([^']+)'.*");

    public TransactionsResponse(String body) {
        super(body);
        this.navigation = getUpdateDocument(JsfPart.CARD_TRANSACTIONS_NAVIGATION);
        this.transactions = getUpdateDocument(JsfPart.CARD_TRANSACTIONS);
    }

    private Optional<String> getPreviousMonthJsfSource() {
        // link is on onclick attribute in first <li> tag. in the last page
        final String script =
                evaluateXPath(
                        navigation, "//ul/li[1]/a[contains(@onclick, '')]/@onclick", String.class);

        if (script.isEmpty()) {
            return Optional.empty();
        }

        final Matcher matcher = JSF_SOURCE_PATTERN.matcher(script);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        } else {
            return Optional.empty();
        }
    }

    public PaginationKey getNextKey() {
        final Optional<String> source = getPreviousMonthJsfSource();
        return source.map(value -> new PaginationKey(value, getViewState())).orElse(null);
    }

    private NodeList getTransactionRows() {
        return evaluateXPath(
                transactions, "//table[contains(@class,'tableSlide')]/tbody/tr", NodeList.class);
    }

    private boolean hasNoTransactions() {
        if (getTransactionRows().getLength() == 1) {
            final Node row = getTransactionRows().item(0);
            return evaluateXPath(row, "td[contains(@class,'sinMov')]", Boolean.class);
        }
        return false;
    }

    private Optional<LocalDate> getTransactionDate(Node row) {
        final String dateValue = evaluateXPath(row, "td[1]", String.class).trim();
        if (dateValue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseTransactionDate(dateValue));
    }

    private CreditCardTransaction rowToTransaction(
            Node row, CreditCardAccount account, LocalDate date) {
        final NodeList transactionCells =
                evaluateXPath(row, "td[2]/table/tr[1]/td", NodeList.class);
        if (null == transactionCells || transactionCells.getLength() != 4) {
            throw new IllegalStateException(
                    "Transaction should have 4 cells, but has "
                            + (transactionCells == null ? 0 : transactionCells.getLength()));
        }

        final String description = getDescription(transactionCells);
        final String amount = getAmount(transactionCells);
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(account)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(new AvailableDateInformation(date))
                                        .build())
                        .setDate(date)
                        .setDescription(description)
                        .setAmount(parseAmount(amount))
                        .setPending(false)
                        .setMutable(false)
                        .setProviderMarket(MarketCode.ES.toString())
                        .setRawDetails(this)
                        .build();
    }

    private String getDescription(NodeList transactionCells) {
        return transactionCells.item(0).getTextContent().trim();
    }

    private String getAmount(NodeList transactionCells) {
        return transactionCells.item(3).getTextContent().trim().split("\n")[0];
    }

    public List<CreditCardTransaction> toTinkTransactions(CreditCardAccount account) {
        if (hasNoTransactions()) {
            return Collections.emptyList();
        }

        LocalDate transactionDate = null;
        List<CreditCardTransaction> transactionList = new ArrayList<>();
        final NodeList transactionRows = getTransactionRows();

        for (int i = 0; i < transactionRows.getLength(); i++) {
            final Node row = transactionRows.item(i);
            transactionDate = getTransactionDate(row).orElse(transactionDate);
            transactionList.add(rowToTransaction(row, account, transactionDate));
        }
        return transactionList;
    }
}
