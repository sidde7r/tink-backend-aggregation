package se.tink.libraries.abnamro.utils.tikkie;

import com.google.common.base.Strings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;

public class TikkieUtils {

    private final static Pattern TIKKIE_PATTERN = Pattern.compile(".*Tikkie$", Pattern.CASE_INSENSITIVE);

    // See TikkieUtilsTest for example patterns
    private final static Pattern TIKKIE_INCOME_DETAILS_PATTERN = Pattern
            .compile("^Tikkie ID (\\d| )*,(?<message>.*),(?<name>.*)$", Pattern.CASE_INSENSITIVE);

    // See TikkieUtilsTest for example patterns. "NL\d{2}.*" is the beginning of dutch IBAN numbers.
    private final static Pattern TIKKIE_EXPENSE_DETAILS_PATTERN = Pattern
            .compile("(\\d|\\s)*(?<name>.*) NL\\d{2}.*", Pattern.CASE_INSENSITIVE);

    /**
     * Returns true/false if the description ends with "Tikkie". We consider this to be a Tikkie transaction that we
     * later can extract message and sender/receiver from.
     */
    public static boolean isTikkieTransaction(Transaction transaction) {
        String description = transaction.getDescription();

        return description != null && TIKKIE_PATTERN.matcher(description).matches();
    }

    /**
     * Parse message and name of the sender or the receiver. Null if we couldn't parse anything.
     */
    public static TikkieDetails parseTransactionDetails(Transaction transaction) {

        String payloadMessage = transaction.getPayloadValue(TransactionPayloadTypes.MESSAGE);

        if (payloadMessage == null) {
            return null;
        }

        if (transaction.getAmount() > 0) {
            return parseIncomeDetails(payloadMessage);
        } else {
            return parseExpenseDetails(payloadMessage);
        }
    }

    private static TikkieDetails parseIncomeDetails(String payloadMessage) {
        Matcher matcher = TIKKIE_INCOME_DETAILS_PATTERN.matcher(payloadMessage);

        if (matcher.find()) {

            String name = Strings.nullToEmpty(matcher.group("name")).trim();
            String message = Strings.nullToEmpty(matcher.group("message")).trim();

            if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(message)) {
                return null;
            }

            return new TikkieDetails(name, message);

        }

        return null;
    }

    private static TikkieDetails parseExpenseDetails(String payloadMessage) {
        Matcher matcher = TIKKIE_EXPENSE_DETAILS_PATTERN.matcher(payloadMessage);

        if (matcher.find()) {

            String name = Strings.nullToEmpty(matcher.group("name")).trim();

            if (Strings.isNullOrEmpty(name)) {
                return null;
            }

            return new TikkieDetails(name, null);
        }

        return null;
    }
}
