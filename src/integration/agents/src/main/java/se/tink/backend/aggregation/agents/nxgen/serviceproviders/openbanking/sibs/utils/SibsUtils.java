package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SibsUtils {

    private static final String DASH = "-";

    private SibsUtils() {}

    public static String getDigest(Object body) {
        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        return Hash.sha256Base64(bytes);
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString().replace(DASH, StringUtils.EMPTY);
    }

    public static Retryer<SibsTransactionStatus> getPaymentStatusRetryer(
            long sleepTime, int retryAttempts) {
        return RetryerBuilder.<SibsTransactionStatus>newBuilder()
                .retryIfResult(status -> status != null && status.isWaitingStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    public static Retryer<ConsentStatus> getConsentStatusRetryer(
            long sleepTime, int retryAttempts) {
        return RetryerBuilder.<ConsentStatus>newBuilder()
                .retryIfResult(status -> status != null && !status.isFinalStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }
}
