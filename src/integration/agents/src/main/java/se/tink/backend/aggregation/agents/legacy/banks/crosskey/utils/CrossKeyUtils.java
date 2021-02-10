package se.tink.backend.aggregation.agents.banks.crosskey.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.util.List;
import org.joda.time.DateTime;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.utils.TransactionOrdering;
import se.tink.libraries.strings.StringUtils;

public class CrossKeyUtils {
    public static final String SUPPLEMENTAL_RESPONSE_NAME = "response";

    public static String generateUdIdFor(String username) {
        return StringUtils.hashAsUUID("TINK-" + username);
    }

    public static Field[] createOneTimeCodeChallengeFields(String challenge) {
        Field challengeField =
                Field.builder()
                        .immutable(true)
                        .description("Engångskod")
                        .value(challenge)
                        .name("challenge")
                        .helpText(
                                "Ange koden från ditt kodhäfte, dubbelkolla så att koden du skriver in har rätt plats i kodhäftet")
                        .build();

        Field responseField =
                Field.builder()
                        .description("Engångskod")
                        .name(SUPPLEMENTAL_RESPONSE_NAME)
                        .numeric(true)
                        .hint("NNNN")
                        .maxLength(4)
                        .minLength(4)
                        .pattern("([0-9]{4})")
                        .build();

        return new Field[] {challengeField, responseField};
    }

    /**
     * The purpose of this function is to remove all spaces As well as &nbsp (Non-breaking spaces)
     */
    public static String removeSpaces(String string) {
        return string.replaceAll("\\s+|\\u00A0", "");
    }

    /**
     * @param previousLowerBound should always be the first of a month
     * @return Next older range to page
     */
    public static Range<DateTime> getNextPage(DateTime previousLowerBound) {
        Preconditions.checkArgument(
                previousLowerBound.getDayOfMonth() == 1,
                "Since this is called with previous lower bound we should always be on 1st of month");

        DateTime upper = previousLowerBound.minusDays(1); // 2017-05-01 --> 2017-04-30
        DateTime lower = previousLowerBound.minusMonths(2); // 2017-05-01 --> 2017-03-01

        return Range.closed(lower, upper);
    }

    /** @param transactions non-empty list */
    public static Range<DateTime> getDateRange(List<Transaction> transactions) {
        Preconditions.checkArgument(
                !transactions.isEmpty(), "Not expecting call without elements in list");

        Transaction mostRecentTransaction =
                transactions.stream().max(TransactionOrdering.TRANSACTION_DATE_ORDERING).get();
        Transaction oldestTransaction =
                transactions.stream().min(TransactionOrdering.TRANSACTION_DATE_ORDERING).get();

        return Range.closed(
                new DateTime(oldestTransaction.getDate()),
                new DateTime(mostRecentTransaction.getDate()));
    }

    public static Range<DateTime> getFirstPage(DateTime mostRecentTransactionDate) {
        DateTime lower =
                mostRecentTransactionDate
                        .minusMonths(2)
                        .dayOfMonth()
                        .withMaximumValue() // 2017-05-10 --> 2017-03-31
                        .plusDays(1);

        return Range.closed(lower, mostRecentTransactionDate);
    }
}
