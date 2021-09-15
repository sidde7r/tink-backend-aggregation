package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class FrOpenBankingPaymentDatePolicy {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public LocalDate apply(Payment payment) {
        return FrOpenBankingDateUtil.getExecutionDate(payment.getExecutionDate());
    }

    public String getExecutionDateWithBankTimeZone(Payment payment) {
        // some banks don't accept date at start day, adding one minute solves the issue
        return apply(payment)
                .atStartOfDay(ZoneId.of("CET"))
                .plusMinutes(1)
                .format(DATE_TIME_FORMATTER);
    }
}
