package se.tink.libraries.request_tracing;

import com.google.common.annotations.VisibleForTesting;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.MDC;

public class RequestTracer {
    @VisibleForTesting static final int REQUEST_ID_LENGTH = 32;
    @VisibleForTesting static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final Random random = new Random(new SecureRandom().nextLong());

    public static String startTracing(Optional<String> requestId) {
        String id = requestId.orElse(generateRequestId());
        MDC.put(REQUEST_ID_MDC_KEY, id);
        return id;
    }

    public static void stopTracing() {
        MDC.remove(REQUEST_ID_MDC_KEY);
    }

    public static String getRequestId() {
        return MDC.get(REQUEST_ID_MDC_KEY);
    }

    private static String generateRequestId() {
        final byte[] randomBytes = new byte[REQUEST_ID_LENGTH / 2];
        random.nextBytes(randomBytes);
        return Hex.encodeHexString(randomBytes);
    }
}
