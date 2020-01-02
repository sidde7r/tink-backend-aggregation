package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public class TransactionMatcher extends TypeSafeMatcher<Transaction> {
    private static final SimpleDateFormat TRANSACTION_DATE_FORMATTER =
            new SimpleDateFormat("dd/MM/yyyy");

    final String dateString;
    final String description;
    final double amountInEur;

    public TransactionMatcher(String date, String description, double eur) {
        this.dateString = date;
        this.description = Strings.nullToEmpty(description);
        this.amountInEur = eur;
    }

    @Override
    protected boolean matchesSafely(Transaction transaction) {
        return Strings.nullToEmpty(transaction.getDescription()).equals(description)
                && transaction.getAmount().equals(Amount.inEUR(amountInEur))
                && TRANSACTION_DATE_FORMATTER.format(transaction.getDate()).equals(dateString);
    }

    @Override
    protected void describeMismatchSafely(Transaction item, Description mismatchDescription) {
        mismatchDescription.appendText(
                String.format(
                        "was (%s, '%s', %g %s)",
                        TRANSACTION_DATE_FORMATTER.format(item.getDate()),
                        item.getDescription(),
                        item.getAmount().getValue(),
                        item.getAmount().getCurrency()));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(
                String.format("(%s, '%s', %g EUR)", dateString, this.description, amountInEur));
    }
}
