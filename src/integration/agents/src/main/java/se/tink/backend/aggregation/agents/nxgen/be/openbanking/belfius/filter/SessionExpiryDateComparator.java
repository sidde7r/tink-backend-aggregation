package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SessionExpiryDateComparator {

    static String getSessionExpiryInfo(Date sessionExpiryDate) {
        if (sessionExpiryDate == null) {
            return null;
        }
        return String.valueOf(isSessionExpiredPrematurely(sessionExpiryDate));
    }

    private static boolean isSessionExpiredPrematurely(Date sessionExpiryDate) {
        return new Date(System.currentTimeMillis()).before(sessionExpiryDate);
    }
}
