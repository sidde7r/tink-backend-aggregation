package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.ActivityListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.Message;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AmericanExpressV62Predicates {

    public static final Function<ActivityListEntity, List<TransactionEntity>> getTransactionsFromGivenPage = activity ->
            Optional.ofNullable(activity.getTransactionList())
                    // If we don't have more pages, we return true for `canStillFetch` and use it
                    // in paginator
                    .orElse(Lists.emptyList());
    private static final String ERROR = "ERROR";
    private static final Logger LOGGER = LoggerFactory.getLogger(AmericanExpressV62Predicates.class);
    // Cancelled card contains Message with type ERROR
    public static final Predicate<CardEntity> cancelledCardsPredicate = c -> {
        Message m = c.getMessage();
        if (m != null && ERROR.equalsIgnoreCase(m.getType())) {
            // TODO: Not sure if should be warning or just info
            LOGGER.warn("Credit card not included because of error message: " + m.getShortValue());
            return false;
        }
        return true;
    };
    public static final Predicate<CardEntity> cancelledCardSummaryValuePredicate = c -> {
        if ("true".equals(c.getCanceled())) {
            return false;
        }
        return true;
    };
    public static final Consumer<TransactionEntity> transformIntoTinkTransactions(
            AmericanExpressV62Configuration config,
            List<Transaction> list) {
        return transaction -> list.add(transaction.toTransaction(config, false));
    }
}
