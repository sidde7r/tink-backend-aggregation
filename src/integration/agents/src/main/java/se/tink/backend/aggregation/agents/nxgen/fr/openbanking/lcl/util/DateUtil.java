package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;

public class DateUtil {
    private DateUtil() {}

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter SAME_DAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static String plusOneDayDate(String date) {
        return ZonedDateTime.parse(date).plusDays(1L).format(DATE_TIME_FORMATTER);
    }

    public static String getExecutionDate(CreatePaymentRequest paymentRequest) {
        AccountEntity creditorAccount = paymentRequest.getBeneficiary().getCreditorAccount();
        if (creditorAccount.isFrenchIban() || creditorAccount.isMonacoIban()) {
            return DateUtil.plusOneDayDate(paymentRequest.getRequestedExecutionDate());
        } else {
            return ZonedDateTime.parse(paymentRequest.getCreationDateTime())
                    .withZoneSameInstant(ZoneId.of("GMT"))
                    .format(SAME_DAY_FORMATTER);
        }
    }
}
