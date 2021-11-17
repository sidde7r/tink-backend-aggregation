package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SibsUtils {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(SibsConstants.Formats.CONSENT_BODY_DATE_FORMAT);
    private static final String DASH = "-";
    private static final DateTimeFormatter TRANSACTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(SibsConstants.Formats.TRANSACTION_DATE_FORMAT);

    private SibsUtils() {}

    public static String getDigest(Object body) {
        String stringBody = SerializationUtils.serializeToString(body);
        byte[] bytes = stringBody.getBytes(StandardCharsets.UTF_8);
        return Hash.sha256Base64(bytes);
    }

    public static String get90DaysValidConsentStringDate(LocalDateTimeSource localDateTimeSource) {
        LocalDateTime now = localDateTimeSource.now();
        LocalDateTime days90Later = now.plusDays(90);
        return CONSENT_BODY_DATE_FORMATTER.format(days90Later);
    }

    public static String getRequestId(RandomValueGenerator randomValueGenerator) {
        return randomValueGenerator.getUUID().toString().replace(DASH, StringUtils.EMPTY);
    }

    public static Retryer<SibsTransactionStatus> getPaymentStatusRetryer(
            long sleepTime, int retryAttempts) {
        return RetryerBuilder.<SibsTransactionStatus>newBuilder()
                .retryIfResult(status -> status != null && status.isWaitingStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    public static LocalDate convertStringToLocalDate(String localDate) {
        return (StringUtils.isEmpty(localDate))
                ? null
                : LocalDate.parse(localDate, TRANSACTION_DATE_FORMATTER);
    }

    public static String convertLocalDateToString(LocalDate localDate) {
        return (localDate == null) ? null : TRANSACTION_DATE_FORMATTER.format(localDate);
    }
}
