package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RequiredArgsConstructor
class SessionExpiryDateComparator {

    private final LocalDateTimeSource localDateTimeSource;

    String getSessionExpiryInfo(Date sessionExpiryDate) {
        if (sessionExpiryDate == null) {
            return null;
        }
        return String.valueOf(isSessionExpiredPrematurely(sessionExpiryDate));
    }

    private boolean isSessionExpiredPrematurely(Date sessionExpiryDate) {
        return localDateTimeSource.getSystemCurrentTimeMillis() < sessionExpiryDate.getTime();
    }
}
