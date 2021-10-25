package se.tink.backend.aggregation.nxgen.http.log.executor.raw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;

@Slf4j
@RequiredArgsConstructor
public class RawHttpTrafficLogger {

    private static final LogTag LOG_TAG = LogTag.from("[RawHttpTrafficLogger]");

    @Getter private final OutputStream loggingOutputStream;
    @Getter private final PrintStream loggingPrintStream;

    public static Optional<RawHttpTrafficLogger> consoleOutputLogger() {
        try {
            OutputStream outputStream = System.out;
            PrintStream printStream = createPrintStream(outputStream);

            return Optional.of(new RawHttpTrafficLogger(outputStream, printStream));
        } catch (Exception e) {
            log.error("{} Could not construct LoggingExecutor", LOG_TAG, e);
            return Optional.empty();
        }
    }

    public static Optional<RawHttpTrafficLogger> inMemoryLogger() {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = createPrintStream(outputStream);

            return Optional.of(new RawHttpTrafficLogger(outputStream, printStream));
        } catch (Exception e) {
            log.error("{} Could not construct LoggingExecutor", LOG_TAG, e);
            return Optional.empty();
        }
    }

    private static PrintStream createPrintStream(OutputStream outputStream)
            throws UnsupportedEncodingException {
        return new PrintStream(outputStream, true, "UTF-8");
    }

    /**
     * Use this method to safely store HTTP traffic
     *
     * @param logContent - content of HTTP request/response
     * @param logMasker - log masker to mask sensitive values
     * @param loggingMode - logging mode
     */
    public void log(String logContent, LogMasker logMasker, LoggingMode loggingMode) {
        if (LoggingMode.LOGGING_MASKER_COVERS_SECRETS.equals(loggingMode)) {
            loggingPrintStream.print(logMasker.mask(logContent));
        }
    }

    /**
     * UNSAFE
     *
     * <p>Do NOT use unless you're sure you don't have to mask the log content. This method exists
     * to allow logging some additional things, not necessarily http traffic in "AAP like" format,
     * e.g. HTML content of screen scraped websites.
     *
     * <p>NOTE: In future, when we will need to log additional stuff, it would be better to prepare
     * a separate logger to avoid mixing raw ("AAP like logs") with some other arbitrary data.
     *
     * @param logContentWithoutSensitiveValues - content of the log that has no sensitive values
     */
    public void logRawUnsafe(String logContentWithoutSensitiveValues) {
        loggingPrintStream.print(logContentWithoutSensitiveValues);
    }

    public Optional<String> tryGetLogContent() {
        try {
            return Optional.of(getLogContentInternal());

        } catch (IOException | RuntimeException e) {
            log.error("{} Could not read raw http logs", LOG_TAG, e);
        }
        return Optional.empty();
    }

    private String getLogContentInternal() throws UnsupportedEncodingException {
        if (!(loggingOutputStream instanceof ByteArrayOutputStream)) {
            throw new IllegalStateException(
                    String.format(
                            "%s Attempt to read unsupported stream type: %s",
                            LOG_TAG, loggingOutputStream.getClass()));
        }

        ByteArrayOutputStream inMemoryLogOutputStream = (ByteArrayOutputStream) loggingOutputStream;
        return inMemoryLogOutputStream.toString(StandardCharsets.UTF_8.name());
    }
}
