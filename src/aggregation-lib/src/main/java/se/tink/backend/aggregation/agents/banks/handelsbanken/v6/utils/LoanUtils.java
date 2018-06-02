package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils;

import com.google.common.base.Objects;
import java.util.Date;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LoanEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.Property;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.Segment;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class LoanUtils {

    private static final AggregationLogger log = new AggregationLogger(LoanUtils.class);

    public static Integer parseNumMonthBound(LoanEntity loanEntity) {

        try {
            if (loanEntity.getFixationdateText() != null && loanEntity.getFixationdateText().indexOf(' ') != -1) {
                String[] parts = loanEntity.getFixationdateText().split(" ");
                if (parts.length == 2 && "år".equalsIgnoreCase(parts[1])) {
                    return Integer.parseInt(parts[0]) * 12;
                } else if (parts.length == 2) {
                    return Integer.parseInt(parts[0]);
                }
            } else if ("rörligt".equals(loanEntity.getFixationdateText())) {
                return 3;
            }
        } catch (Exception e) {
            log.warn("Was not able to parse binding period of SHB loan.", e);
        }

        return null;
    }

    public static Date parseNextDayOfTermsChange(LoanEntity loanEntity) {

        try {
            Segment s;
            Property p;

            if ((s = getSegment(loanEntity, "låneinformation")) != null) {
                if ((p = getProperty(s, "villkorsändringsdag")) != null) {
                    if (p.getValue() != null && p.getValue().length() == 10) {
                        return ThreadSafeDateFormat.FORMATTER_DAILY.parse(p.getValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Was not able to parse next date of terms change of SHB loan.", e);
        }

        return null;
    }

    public static Segment getSegment(LoanEntity loan, String title) {
        if (loan.getSegments() != null) {
            for (Segment s : loan.getSegments()) {
                if (title.equalsIgnoreCase(s.getTitle())) {
                    return s;
                }
            }
        }
        return null;
    }

    public static Property getProperty(Segment segment, String label) {
        if (segment != null && segment.getProperties() != null) {
            for (Property p : segment.getProperties()) {
                if (label.equalsIgnoreCase(p.getLabel())) {
                    return p;
                }
            }
        }
        return null;
    }

    public static boolean hasCoApplicants(LoanEntity loanEntity) {
        Segment segment = getSegment(loanEntity, "låneinformation");
        Property multipleApplicants = getProperty(segment, "fler låntagare finns");
        if (multipleApplicants != null) {
            if (Objects.equal(multipleApplicants.getValue(), "ja")) {
                return true;
            } else {
                log.info("Other value than \"ja\" available " + multipleApplicants.getValue());
                return false;
            }
        } else {
            return false;
        }
    }

    public static Double getAmortization(LoanEntity loanEntity) {
        Segment segment = getSegment(loanEntity, "låneinformation");
        Property amortization = getProperty(segment, "amortering");
        if (amortization != null && amortization.getValue() != null) {
            if (Objects.equal(amortization.getValue(), "amorteringsfritt")) {
                return 0.0;
            } else {
                return AgentParsingUtils.parseAmount(amortization.getValue());
            }
        } else {
            log.warn("Was not able to return amortization because of null");
            return null;
        }
    }
}
