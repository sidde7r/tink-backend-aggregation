package se.tink.backend.aggregation.log;

import com.google.api.client.util.Charsets;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.libraries.transfer.iface.UuidIdentifiable;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Extension of AggregationLogger to provide custom formatter for Credentials
 *
 * <p>The methods for (String, String, String) and similar are deprecated, and should be converted
 * to type-safe alternatives.
 *
 * @deprecated Please depend on slf4j directly instead of AggregationLoggger.
 *     https://tink.slack.com/archives/CB12SB8DV/p1592400385429200
 *     https://tink.slack.com/archives/C239US7C5/p1525431375000316
 */
@Deprecated
public class AggregationLogger {
    protected Logger log;

    @VisibleForTesting static final int EXTRA_LONG_LIMIT = 1550;

    public AggregationLogger(Class clazz) {
        this.log = LoggerFactory.getLogger(clazz);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void error(String message) {
        log.error(message);
    }

    public void error(String message, Throwable e) {
        log.error(message, e);
    }

    public void info(String message) {
        log.info(message);
    }

    public void info(String message, Throwable e) {
        log.info(message, e);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void warn(String message, Throwable e) {
        log.warn(message, e);
    }

    private static String concatenate(String message, Exception exception) {
        StringWriter exceptionStackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(exceptionStackTrace));
        return message + "\n" + exceptionStackTrace;
    }

    public void infoExtraLong(String message, LogTag logTag) {
        logExtraLong(message, logTag, this::info);
    }

    public void infoExtraLong(String message, LogTag logTag, Exception exception) {
        logExtraLong(concatenate(message, exception), logTag, this::info);
    }

    @VisibleForTesting
    static void logExtraLong(String message, LogTag logTag, Consumer<String> logger) {
        // Hack to handle max characters 2048 in logging message
        UUID uuid = UUID.randomUUID();
        String messageHeader =
                String.format("%s - %s - counter: COUNTER - message: ", logTag, uuid);
        List<String> loggableStrings = toLoggableStrings(messageHeader, message);
        int totalSize = loggableStrings.size();
        for (int i = 0; i < totalSize; i++) {
            logger.accept(
                    messageHeader.replaceAll("COUNTER", (i + 1) + "/" + totalSize)
                            + loggableStrings.get(i));
        }
    }

    private static List<String> toLoggableStrings(String messageHeader, String message) {
        if (StringUtils.isBlank(message)) {
            return Collections.singletonList("Logging empty message.");
        }
        ImmutableList.Builder<String> loggableStrings = ImmutableList.builder();
        int messageLength = EXTRA_LONG_LIMIT - messageHeader.getBytes(Charsets.UTF_8).length;
        char[] chars = message.toCharArray();
        int substringLength = 0;
        int substringStart = 0;
        for (char aChar : chars) {
            if (aChar > 255) {
                substringLength++;
            }

            if (substringLength >= messageLength) {
                loggableStrings.add(subString(chars, substringLength, substringStart));
                substringStart += substringLength;
                substringLength = 0;
            }

            substringLength++;
        }
        loggableStrings.add(subString(chars, substringLength, substringStart));
        return loggableStrings.build();
    }

    private static String subString(char[] chars, int length, int start) {
        return new String(Arrays.copyOfRange(chars, start, start + length));
    }

    private String format(UuidIdentifiable transfer) {
        return "[transferId: " + UUIDUtils.toTinkUUID(transfer.getId()) + "] ";
    }

    public void info(UuidIdentifiable transfer, String message) {
        this.log.info(format(transfer) + message);
    }

    public void error(UuidIdentifiable transfer, String message, Throwable e) {
        this.log.error(format(transfer) + message, e);
    }
}
