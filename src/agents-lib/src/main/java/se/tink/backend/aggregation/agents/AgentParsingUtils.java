package se.tink.backend.aggregation.agents;

import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import se.tink.backend.aggregation.agents.utils.typeguesser.TypeGuesser;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.strings.StringUtils;

public class AgentParsingUtils {

    protected static final int SAFETY_THRESHOLD_NUMBER_OF_DAYS = 10;
    protected static final int SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS = 10;

    public static String cleanDescription(String text) {
        StringBuilder result = new StringBuilder();
    
        for (char c : text.toCharArray()) {
            if (Character.getType(c) == Character.CONTROL) {
                continue;
            }
    
            result.append(c);
        }
    
        return result.toString();
    }
    
    public static Integer parseNumMonthsBound(String periodDescription) {
        if (periodDescription != null && periodDescription.indexOf(' ') != -1) {
            String[] parts = periodDescription.split(" ");
            if (parts.length == 2 && "år".equalsIgnoreCase(parts[1])) {
                return Integer.parseInt(parts[0]) * 12;
            } else if (parts.length == 2 && "månader".equalsIgnoreCase(parts[1])) {
                return Integer.parseInt(parts[0]);
            } else if (parts.length == 2 && "mån".equalsIgnoreCase(parts[1])) {
                return Integer.parseInt(parts[0]);
            }
        }
        return null;
    }

    public static AccountTypes guessAccountType(Account account) {
        // backwards compatibility, only swedish supported
        return TypeGuesser.SWEDISH.guessAccountType(account.getName());
    }

    public static Double parsePercentageFormInterest(String interestString) {
        if (Strings.isNullOrEmpty(interestString)) {
            return null;
        }

        // Using BigDecimal for the division to not end up with stuff like 0.016200000000000003
        BigDecimal interest = new BigDecimal(parseAmount(interestString.replace("%", "")));
        interest = interest.divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP);

        return interest.doubleValue();
    }
    
    public static double parseAmountTrimCurrency(String text) {
        text = text.toLowerCase();
        text = text.replace("kr", "");
        text = text.replace("sek", "");
        text = text.replace("eur", "");
        return parseAmount(text);
    }
    
    public static double parseAmount(String text) {
        // Remove `Â`, if we received incorrect encoded amount:
        // https://stackoverflow.com/questions/1461907/html-encoding-issues-%C3%82-character-showing-up-instead-of-nbsp
        if (text != null) {
            text = text.replaceAll("Â", "");
        }
        return StringUtils.parseAmount(text);
    }

    public static Date parseDate(String text, boolean flattenTime) {
        if (flattenTime) {
            return DateUtils.flattenTime(DateUtils.parseDate(text));
        } else {
            return DateUtils.parseDate(text);
        }
    }

    public AgentParsingUtils() {
        super();
    }

}
