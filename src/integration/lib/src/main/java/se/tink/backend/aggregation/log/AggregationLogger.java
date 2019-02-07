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
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Extension of AggregationLogger to provide custom formatter for
 * - {@link Credentials}
 *
 * The methods for (String, String, String) and similar are deprecated, and should be converted to type-safe
 * alternatives.
 */
public class AggregationLogger {
    protected Logger log;

    @VisibleForTesting
    static final int EXTRA_LONG_LIMIT = 1550;

    public AggregationLogger(Class clazz) {
        this.log = LoggerFactory.getLogger(clazz);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void debug(String message, Throwable e) {
        log.debug(message, e);
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

    public void trace(String message) {
        log.trace(message);
    }

    public void trace(String message, Throwable e) {
        log.trace(message, e);
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

    public void warnExtraLong(String message, LogTag logTag) {
        logExtraLong(message, logTag, this::warn);
    }

    public void warnExtraLong(String message, LogTag logTag, Exception exception) {
        logExtraLong(concatenate(message, exception), logTag, this::warn);
    }

    public void warnExtraLong(LogTag logTag, HttpResponseException exception) {
        warnExtraLong("Http Response Exception: ", logTag, exception);
    }

    public void errorExtraLong(String message, LogTag logTag, Exception exception) {
        logExtraLong(concatenate(message, exception), logTag, this::error);
    }

    @VisibleForTesting
    static void logExtraLong(String message, LogTag logTag, Consumer<String> logger) {
        // Hack to handle max characters 2048 in logging message
        UUID uuid = UUID.randomUUID();
        String messageHeader = String.format("%s - %s - counter: COUNTER - message: ", logTag, uuid);
        List<String> loggableStrings = toLoggableStrings(messageHeader, message);
        int totalSize = loggableStrings.size();
        for (int i = 0; i < totalSize; i++) {
            logger.accept(
                    messageHeader.replaceAll("COUNTER", (i + 1) +  "/" + totalSize) +
                            loggableStrings.get(i)
            );
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

    private String format(Transfer transfer) {
        return "[transferId: " + UUIDUtils.toTinkUUID(transfer.getId()) + "] ";
    }

    public void info(Transfer transfer, String message) {
        this.log.info(format(transfer) + message);
    }

    public void error(Transfer transfer, String message) {
        this.log.error(format(transfer) + message);
    }

    public void error(Transfer transfer, String message, Throwable e) {
        this.log.error(format(transfer) + message, e);
    }

    public void info(Account account, String message) {
        this.log.info(String.format("[accountId: %s] %s", account.getId(), message));
    }

    private String format(GenericApplication application, String message) {
        return "[applicationId: " + UUIDUtils.toTinkUUID(application.getApplicationId()) + "] " + message;
    }

    public void info(GenericApplication application, String message) {
        this.log.info(format(application, message));
    }

    public void error(GenericApplication application, String message) {
        this.log.error(format(application, message));
    }

    public void error(GenericApplication application, String message, Throwable e) {
        this.log.error(format(application, message), e);
    }

    public void debug(GenericApplication application, String message) {
        debug(String.format("[userId:%s applicationId:%s] %s", UUIDUtils.toTinkUUID(application.getUserId()),
                UUIDUtils.toTinkUUID(application.getApplicationId()), message));
    }
}
