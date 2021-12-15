package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class ICSTimeProvider {

    @Getter private final LocalDateTimeSource localDateTimeSource;

    private final PersistentStorage persistentStorage;

    public LocalDate now() {
        return localDateTimeSource.now(TimeZone.getDefault().toZoneId()).toLocalDate();
    }

    // Can fetch transactions max 3 years back
    public Date getFromDate() {
        return convertToDate(now().minus(3, YEARS));
    }

    public Date getToAndExpiredDate() {
        return convertToDate(now().plus(89, DAYS));
    }

    public String getLastLoggedTime() {
        return new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss z").format(convertToDate(now()));
    }

    /* Earliest transaction date set in consent. This will not exist for users who logged in before
    it was implemented; a max value is used and the error is handled in ICSCreditCardFetcher */
    public Date getConsentTransactionDate() {
        return persistentStorage
                .get(StorageKeys.TRANSACTION_FROM_DATE, Date.class)
                .orElseGet(this::getFallbackFromDate);
    }

    public LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /* Fallback from date used when user doesn't have a date in persistent storage.
    Maximum is 2 years and 89 days, since previously consents were created for 2 years. */
    private Date getFallbackFromDate() {
        return convertToDate(now().minus(2, YEARS).minus(89, DAYS));
    }
}
