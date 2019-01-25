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
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Extension of AggregationLogger to provide custom formatter for
 * - {@link Credentials}
 *
 * The methods for (String, String, String) and similar are deprecated, and should be converted to type-safe
 * alternatives.
 */
public class AggregationLogger extends se.tink.libraries.log.LogUtils {

    @VisibleForTesting
    static final int EXTRA_LONG_LIMIT = 1550;

    public AggregationLogger(Class clazz) {
        super(clazz);
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

    /*
     * {@link Transfer} loggers
     *
     * Transfer is still a part of :main-api, but this will change shortly
     */

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

    /*
     * {@link Account} loggers
     *
     * Account is still a part of :main-api, but this will change shortly
     */

    public void info(Account account, String message) {
        this.log.info(String.format("[accountId: %s] %s", account.getId(), message));
    }

    /*
     * {@link GenericApplication}, {@link Credentials} loggers
     */

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
