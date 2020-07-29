package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities;

import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Loans;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class SELoanProperty {

    private static final AggregationLogger logger = new AggregationLogger(SELoanProperty.class);

    private String label;
    private String value;

    public boolean isTermsOfChange() {
        return hasLabel(Loans.TERMS_OF_CHANGE);
    }

    public boolean isAmortization() {
        return hasLabel(Loans.AMORTIZATION) && !Loans.NO_AMORTIZATION_2.equalsIgnoreCase(value);
    }

    public boolean isMultipleApplicants() {
        return hasLabel(Loans.MULTIPLE_APPLICANTS);
    }

    private boolean hasLabel(String text) {
        return text.equalsIgnoreCase(label);
    }

    public Date asDate() {
        if (value == null) {
            throw new IllegalStateException(
                    "Was not able to return next date of terms change because of null");
        }
        try {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Was not able to parse next date of terms change of SHB loan.", e);
        }
    }

    public Double asAmortizationValue() {
        if (value == null) {
            throw new IllegalStateException("Was not able to return amortization because of null");
        }
        if (Loans.NO_AMORTIZATION.equalsIgnoreCase(value)) {
            return 0.0;
        }
        return AgentParsingUtils.parseAmount(value);
    }

    public boolean asMultipleApplicantValue() {
        boolean isJa = Loans.YES.equalsIgnoreCase(value);
        if (!isJa) {
            logger.warn(String.format("Other value than \"ja\" found: %s", value));
        }
        return isJa;
    }
}
