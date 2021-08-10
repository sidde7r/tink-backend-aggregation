package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentDater {

    public String createDateForHeader() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
