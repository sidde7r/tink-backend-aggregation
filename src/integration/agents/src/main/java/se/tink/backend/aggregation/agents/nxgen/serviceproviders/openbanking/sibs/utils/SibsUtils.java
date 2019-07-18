package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SibsUtils {

    private static final String DASH = "-";
    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    private SibsUtils() {}

    public static String getSigningString(
            String digest, String transactionId, String requestId, String signatureStringDate) {

        StringBuilder signingString = new StringBuilder();

        if (!Strings.isNullOrEmpty(digest)) {
            signingString
                    .append(HeaderKeys.DIGEST.toLowerCase())
                    .append(COLON_SPACE)
                    .append(HeaderValues.DIGEST_PREFIX)
                    .append(digest)
                    .append(NEW_LINE);
        }

        signingString
                .append(HeaderKeys.TPP_TRANSACTION_ID.toLowerCase())
                .append(COLON_SPACE)
                .append(transactionId)
                .append(NEW_LINE)
                .append(HeaderKeys.TPP_REQUEST_ID.toLowerCase())
                .append(COLON_SPACE)
                .append(requestId)
                .append(NEW_LINE)
                .append(HeaderKeys.DATE.toLowerCase())
                .append(COLON_SPACE)
                .append(signatureStringDate);

        return signingString.toString();
    }

    public static String getDigest(Object body) {
        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        return Hash.sha256Base64(bytes);
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString().replace(DASH, StringUtils.EMPTY);
    }

    public static Retryer<ConsentStatus> getConsentStatusRetryer(
            long sleepTime, int retryAttempts) {
        return RetryerBuilder.<ConsentStatus>newBuilder()
                .retryIfResult(status -> status != null && status.isWaitingStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }
}
