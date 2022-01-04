package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class BpceGroupPaymentDatePolicy extends FrOpenBankingPaymentDatePolicy {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");
    private final boolean isCaisseGroup;

    public BpceGroupPaymentDatePolicy(String providerName) {
        isCaisseGroup = providerName.startsWith("fr-caisse");
    }

    @Override
    public LocalDate apply(Payment payment) {
        if (payment.getExecutionDate() != null) {
            return payment.getExecutionDate();
        } else {
            LocalDate createDate = FrOpenBankingDateUtil.getCreationDate().toLocalDate();
            if (isCaisseGroup && !FrOpenBankingDateUtil.isBusinessDate(createDate)) {
                return super.apply(payment).plusDays(3);
            } else {
                return super.apply(payment);
            }
        }
    }

    @Override
    public String getExecutionDateWithBankTimeZone(Payment payment) {
        if (payment.isSepaInstant()) {
            return getCreationDate()
                    .atZone(DEFAULT_ZONE_ID)
                    .plusMinutes(1)
                    .format(DATE_TIME_FORMATTER);
        } else {
            final LocalDateTime creationDate = getCreationDate();
            final LocalDate appliedDate = apply(payment);

            return appliedDate.equals(creationDate.toLocalDate())
                    ? getExecutionDayWhenAppliedDateEqualsCreationDate(appliedDate, creationDate)
                    : getExecutionDayWhenAppliedDateIsNotEqualToCreationDate(appliedDate);
        }
    }

    private String getExecutionDayWhenAppliedDateEqualsCreationDate(
            LocalDate appliedDate, LocalDateTime creationDate) {
        return appliedDate
                .atTime(creationDate.toLocalTime())
                .atZone(DEFAULT_ZONE_ID)
                .plusMinutes(1)
                .format(DATE_TIME_FORMATTER);
    }

    private String getExecutionDayWhenAppliedDateIsNotEqualToCreationDate(LocalDate appliedDate) {
        return appliedDate.atStartOfDay(DEFAULT_ZONE_ID).plusMinutes(1).format(DATE_TIME_FORMATTER);
    }
}
