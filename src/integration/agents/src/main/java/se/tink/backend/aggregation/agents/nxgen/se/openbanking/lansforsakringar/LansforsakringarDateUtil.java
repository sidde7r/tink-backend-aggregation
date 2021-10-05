package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.rpc.Creditor;

public class LansforsakringarDateUtil {

    private static final int BGPG_CUTOFF_HOURS = 10;
    private static final int BGPG_CUTOFF_MINUTES = 0;
    private static final int A2A_CUTOFF_HOURS = 13;
    private static final int A2A_CUTOFF_MINUTES = 45;
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    private LansforsakringarDateUtil() {}

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    /**
     * If the localDate is null, then find the nearest business date If the input is a concrete,
     * valid date, then use that date
     *
     * @param localDate null or a customer input date
     * @return nearest business date or the same customer input date
     */
    public static LocalDate getCurrentOrNextBusinessDate(LocalDate localDate, Creditor creditor) {
        if (creditor.getAccountIdentifierType() == AccountIdentifierType.SE_BG
                || creditor.getAccountIdentifierType() == AccountIdentifierType.SE_PG) {
            return dateHelper.getProvidedDateOrBestPossibleLocalDate(
                    localDate, BGPG_CUTOFF_HOURS, BGPG_CUTOFF_MINUTES);
        } else {
            return dateHelper.getProvidedDateOrBestPossibleLocalDate(
                    localDate, A2A_CUTOFF_HOURS, A2A_CUTOFF_MINUTES);
        }
    }
}
