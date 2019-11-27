package se.tink.sa.agent.pt.ob.sibs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SibsUtils {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(SibsConstants.Formats.CONSENT_BODY_DATE_FORMAT);

    public static String get90DaysValidConsentStringDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime days90Later = now.plusDays(90);
        return CONSENT_BODY_DATE_FORMATTER.format(days90Later);
    }
}
