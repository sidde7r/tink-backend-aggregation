package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsParser;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class MT940Statement {

    private static final Logger LOGGER = LoggerFactory.getLogger(MT940Statement.class);

    public MT940Statement(String tag_61, String tag_86) {
        this.tag_61 = tag_61;
        this.tag_86 = tag_86;
    }

    private String tag_61;
    private String tag_86;

    public Date getDate() {
        String date = null;
        try {
            date = tag_61.substring(0, 6);
            return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE_COMPACT.parse(date);
        } catch (Exception e) {
            LOGGER.error("{} tag_61: {} date: {}", FinTsConstants.LogTags.DATE_PARSING_ERROR, tag_61, date);
            throw new IllegalStateException("parsing date failed");
        }
    }

    private double getAmount() {
        return FinTsParser.getMT940Amount(tag_61);
    }

    private String getDescription() {
        String[] elements = tag_86.split("\\?");
        // TODO heuristic search for the description until got more samples to look at
        return Arrays.stream(elements).filter(s -> hasField(s, "32")).findFirst().orElse(
                Arrays.stream(elements).filter(s -> hasField(s, "21")).findFirst().orElse(
                        Arrays.stream(elements).filter(s -> hasField(s, "20")).findFirst().orElse(
                                Arrays.stream(elements).filter(s -> hasField(s, "00")).findFirst().orElse("")
                        ))).substring(2);
    }

    public Map<String, String> getRawDetails() {
        HashMap<String, String> result = new HashMap<>();
        String[] elements = tag_86.split("\\?");

        for (String s : elements) {
            if (s.length() < 2) {
                continue;
            }
            String key = s.substring(0, 2);
            String value = s.substring(2);
            result.put(key, value);
        }

        return result;
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(FinTsConstants.CURRENCY, getAmount()))
                .setDate(getDate())
                .setDescription(getDescription())
                .setRawDetails(getRawDetails())
                .build();
    }

    private boolean hasField(String str, String tag) {
        return str.startsWith(tag) && !Strings.isNullOrEmpty(str.substring(2).trim());
    }

}
