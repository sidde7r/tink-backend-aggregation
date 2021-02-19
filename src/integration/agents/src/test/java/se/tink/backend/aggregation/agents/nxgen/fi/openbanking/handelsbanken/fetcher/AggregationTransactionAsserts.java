package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher;

import java.util.Date;
import org.assertj.core.api.AbstractAssert;
import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Ignore
public class AggregationTransactionAsserts
        extends AbstractAssert<AggregationTransactionAsserts, AggregationTransaction> {

    public AggregationTransactionAsserts(AggregationTransaction aggregationTransaction) {
        super(aggregationTransaction, AggregationTransactionAsserts.class);
    }

    public static AggregationTransactionAsserts assertThat(AggregationTransaction actual) {
        return new AggregationTransactionAsserts(actual);
    }

    public AggregationTransactionAsserts hasExactAmount(ExactCurrencyAmount amount) {
        if (!actual.getExactAmount().equals(amount)) {
            failWithMessage(
                    "Expected amount to be <%s>, but was <%s>",
                    amount.toString(), actual.getAmount().toString());
        }
        return this;
    }

    public AggregationTransactionAsserts hasDate(Date date) {
        if (!actual.getDate().equals(date)) {
            failWithMessage("Expected date to be <%t>, but was <%t>", date, actual.getDate());
        }
        return this;
    }

    public AggregationTransactionAsserts hasDescription(String description) {
        if (!actual.getDescription().equals(description)) {
            failWithMessage(
                    "Expected date to be <%s>, but was <%s>", description, actual.getDescription());
        }
        return this;
    }
}
