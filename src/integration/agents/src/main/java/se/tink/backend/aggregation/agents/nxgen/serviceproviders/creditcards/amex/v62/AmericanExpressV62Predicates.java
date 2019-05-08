package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.Message;

public class AmericanExpressV62Predicates {

    public static final Predicate<CardEntity> cancelledCardSummaryValuePredicate =
            c -> {
                return !"true".equals(c.getCanceled());
            };

    public static final Function<String, String> getCardEndingNumbers =
            fullCardNumber -> fullCardNumber.split("-")[1].trim();
    private static final String ERROR = "ERROR";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AmericanExpressV62Predicates.class);

    // Cancelled card contains Message with type ERROR
    public static final Predicate<CardEntity> cancelledCardsPredicate =
            c -> {
                Message m = c.getMessage();
                if (m != null && ERROR.equalsIgnoreCase(m.getType())) {
                    // TODO: Not sure if should be warning or just info
                    LOGGER.warn(
                            "Credit card not included because of error message: "
                                    + m.getShortValue());
                    return false;
                }
                return true;
            };
}
