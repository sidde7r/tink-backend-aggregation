package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.DEFAULT_LOCALE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.DEFAULT_ZONE_ID;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.DOMESTIC_CUT_OFF_HOURS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.DOMESTIC_CUT_OFF_MINUTES;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.GIROS_DOMESTIC_CUT_OFF_HOURS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Date.GIROS_DOMESTIC_CUT_OFF_MINUTES;

import java.time.Clock;
import java.time.LocalDate;
import java.util.TimeZone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentProduct;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.rpc.Payment;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SkandiaDateUtil {

    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static LocalDate getExecutionDate(Payment payment) {
        switch (PaymentProduct.from(payment)) {
            case DOMESTIC_GIROS:
                return getExecutionDateForGirosDomesticPayment(payment.getExecutionDate());
            case DOMESTIC_CREDIT_TRANSFERS:
                return getExecutionDateForDomesticPayment(payment);
            case CROSS_BORDER_CREDIT_TRANSFERS:
            default:
                return payment.getExecutionDate();
        }
    }

    private static LocalDate getExecutionDateForDomesticPayment(Payment payment) {

        AccountIdentifier from = payment.getDebtor().getAccountIdentifier();
        AccountIdentifier to = payment.getCreditor().getAccountIdentifier();

        if (IntraBankChecker.isSwedishMarketIntraBank(from, to)) {
            return getExecutionDateForIntraBankPayment(payment.getExecutionDate());
        } else {
            return getExecutionDateDateForInterBankPayment(payment.getExecutionDate());
        }
    }

    private static LocalDate getExecutionDateForGirosDomesticPayment(LocalDate executionDate) {
        return dateHelper.getProvidedDateOrBestPossibleLocalDate(
                executionDate, GIROS_DOMESTIC_CUT_OFF_HOURS, GIROS_DOMESTIC_CUT_OFF_MINUTES);
    }

    private static LocalDate getExecutionDateForIntraBankPayment(LocalDate executionDate) {
        return dateHelper.getProvidedDateOrCurrentLocalDate(executionDate);
    }

    private static LocalDate getExecutionDateDateForInterBankPayment(LocalDate executionDate) {
        return dateHelper.getProvidedDateOrBestPossibleLocalDate(
                executionDate, DOMESTIC_CUT_OFF_HOURS, DOMESTIC_CUT_OFF_MINUTES);
    }

    static void setClockForTesting(Clock clockForTesting) {
        dateHelper.setClock(clockForTesting);
    }
}
