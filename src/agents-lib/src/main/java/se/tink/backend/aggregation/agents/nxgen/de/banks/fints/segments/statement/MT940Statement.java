package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement;

import java.util.Arrays;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsParser;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class MT940Statement {

    public MT940Statement(String tag_61, String tag_86) {
        this.tag_61 = tag_61;
        this.tag_86 = tag_86;
    }

    private String tag_61;
    private String tag_86;


    public Date getDate() {
        try {
            return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE_COMPACT.parse(tag_61.substring(0, 6));
        } catch (Exception e) {
            throw new IllegalStateException("parsing date failed");
        }
    }

    private double getAmount() {



        return FinTsParser.getMT940Amount(tag_61);
    }

    private String getDescription() {
        String[] elements = tag_86.split("\\?");
        // TODO heuristic search for the description until got more samples to look at
        return Arrays.stream(elements).filter(s -> s.startsWith("32")).findFirst().orElse(
                Arrays.stream(elements).filter(s -> s.startsWith("21")).findFirst().orElse(
                Arrays.stream(elements).filter(s -> s.startsWith("20")).findFirst().orElse(
                        Arrays.stream(elements).filter(s -> s.startsWith("00")).findFirst().orElse("")
                ))).substring(2);
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(FinTsConstants.CURRENCY, getAmount()))
                .setDate(getDate())
                .setDescription(getDescription())
                .build();
    }


}
