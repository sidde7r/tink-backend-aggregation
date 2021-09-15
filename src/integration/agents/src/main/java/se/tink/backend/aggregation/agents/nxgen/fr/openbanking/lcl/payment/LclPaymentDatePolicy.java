package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class LclPaymentDatePolicy extends FrOpenBankingPaymentDatePolicy {

    @Override
    public LocalDate apply(Payment payment) {
        if (payment.getExecutionDate() != null) {
            return payment.getExecutionDate();
        }
        LocalDateTime created = FrOpenBankingDateUtil.getCreationDate();
        return created.plusDays(1L).toLocalDate();
    }

    @Override
    public String getExecutionDateWithBankTimeZone(Payment payment) {
        // some banks don't accept date at start day, adding one minute solves the issue
        // additionally LCL operates in UTC TimeZone
        return apply(payment)
                .atStartOfDay(ZoneId.of("UTC"))
                .plusMinutes(1)
                .format(DATE_TIME_FORMATTER);
    }
}
