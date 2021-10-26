package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentDatePolicy;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.rpc.Payment;

public class BoursoramaPaymentDatePolicy extends FrOpenBankingPaymentDatePolicy {

    private final CountryDateHelper dateHelper;
    private final LocalDateTimeSource localDateTimeSource;

    public BoursoramaPaymentDatePolicy(
            CountryDateHelper dateHelper, LocalDateTimeSource localDateTimeSource) {
        this.dateHelper = dateHelper;
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public String getExecutionDateWithBankTimeZone(Payment payment) {
        if (payment.isSepaInstant()) {
            return ZonedDateTime.of(
                            provideNextPossibleDate(payment),
                            localDateTimeSource
                                    .now(BoursoramaConstants.DEFAULT_ZONE_ID)
                                    .toLocalTime()
                                    .plusMinutes(1),
                            BoursoramaConstants.DEFAULT_ZONE_ID)
                    .format(DATE_TIME_FORMATTER);
        }
        return super.getExecutionDateWithBankTimeZone(payment);
    }

    private LocalDate provideNextPossibleDate(Payment payment) {
        return payment.getExecutionDate() != null
                ? payment.getExecutionDate()
                : dateHelper.getNowAsLocalDate();
    }
}
