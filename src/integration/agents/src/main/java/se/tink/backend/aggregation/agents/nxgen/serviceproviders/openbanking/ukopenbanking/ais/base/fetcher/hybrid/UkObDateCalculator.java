package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid;

import java.time.OffsetDateTime;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaStatus;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range.DateRangeCalculator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@Slf4j
@RequiredArgsConstructor
public class UkObDateCalculator<ACCOUNT extends Account> {

    private final ScaExpirationValidator scaValidator;
    private final DateRangeCalculator<ACCOUNT> calculator;
    private final Period maxPeriodForExpiredSca;
    private final Period maxPeriodForValidSca;

    private ScaStatus scaStatus = ScaStatus.UNKNOWN;

    public OffsetDateTime calculateTo(OffsetDateTime from) {
        return calculator.calculateTo(from);
    }

    public OffsetDateTime calculateFromAsStartOfTheDayWithLimit(
            OffsetDateTime to, Period period, OffsetDateTime limit) {
        return calculator.calculateFromAsStartOfTheDayWithLimit(to, period, limit);
    }

    public OffsetDateTime calculateFinalFromDate(ACCOUNT account, OffsetDateTime toDateTime) {
        evaluateScaStatusIfUnknown();
        OffsetDateTime finalFrom;

        switch (scaStatus) {
            case VALID:
                finalFrom =
                        calculator.calculateFromAsStartOfTheDay(toDateTime, maxPeriodForValidSca);
                log.info(
                        "[TRANSACTION FETCHING] SCA valid. Proposed finalFromDateTime: {}",
                        finalFrom);
                break;
            case EXPIRED:
            default:
                finalFrom =
                        calculator.calculateFromAsStartOfTheDay(toDateTime, maxPeriodForExpiredSca);
                log.info(
                        "[TRANSACTION FETCHING] SCA expired. Proposed finalFromDateTime: {}",
                        finalFrom);
                break;
        }

        OffsetDateTime finalFromDate = calculator.applyCertainDateLimit(account, finalFrom);
        log.info(
                "[TRANSACTION FETCHING] finalFromDateTime after applying certain date limit: {}",
                finalFromDate);

        return finalFromDate;
    }

    private void evaluateScaStatusIfUnknown() {
        if (scaStatus == ScaStatus.UNKNOWN) {
            scaStatus = scaValidator.evaluateStatus();
        }
    }
}
