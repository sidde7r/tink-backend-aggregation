package se.tink.backend.aggregation.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class AggregationLoggerTest {

    private String message;
    private LogTag logTag;
    private TestLogger testLogger;

    @Before
    public void setUp() throws Exception {
        logTag = LogTag.from("log tag");
    }

    @Test
    public void loggingExtraLong_canHandleMessageOverLimit() {
        message = createMessageWithSize(AggregationLogger.EXTRA_LONG_LIMIT + 2);

        logExtraLong();

        testLogger.assertMessageLength(2);
    }

    @Test
    public void loggingExtraLong_canHandleLogTagOverLimit() {
        message =
                createMessageWithSize(
                        AggregationLogger.EXTRA_LONG_LIMIT - logTag.toString().length() + 1);

        logExtraLong();

        testLogger.assertMessageLength(2);
    }

    @Test
    public void loggingExtraLong_canHandleNonAscii() {
        message = "räksmörgås";

        logExtraLong();

        testLogger.assertMessageLength(1);
        testLogger.assertIsContained(message);
    }

    @Test
    public void loggingExtraLong_canHandleNullMessage() {
        message = null;

        logExtraLong();

        testLogger.assertMessageLength(1);
        testLogger.assertIsContained("Logging empty message.");
    }

    @Test
    public void loggingExtraLong_canHandleEmptyMessage() {
        message = "";

        logExtraLong();

        testLogger.assertMessageLength(1);
        testLogger.assertIsContained("Logging empty message.");
    }

    @Test
    public void loggingExtraLong_canHandleBlankMessage() {
        message = "  ";

        logExtraLong();

        testLogger.assertMessageLength(1);
        testLogger.assertIsContained("Logging empty message.");
    }

    private void logExtraLong() {
        testLogger = new TestLogger();

        AggregationLogger.logExtraLong(message, logTag, testLogger);
    }

    private static String createMessageWithSize(int size) {
        return StringUtils.leftPad("a", size, '*');
    }

    private static class TestLogger implements Consumer<String> {

        private List<String> actual = new ArrayList<>();

        @Override
        public void accept(String message) {
            this.actual.add(message);
        }

        public void assertMessageLength(int expected) {
            int size = actual.size();
            assertEquals(actual + " was not of expected size", expected, size);
            for (int i = 0; i < size; i++) {
                String message = actual.get(i);
                assertTrue(
                        "<" + message + "> does not contain expected counter.",
                        message.contains(String.format("counter: %d/%d", i + 1, size)));
            }
        }

        public void assertIsContained(String message) {
            assertTrue(
                    actual + " did not contain: " + message,
                    actual.stream().anyMatch(loggedMessage -> loggedMessage.contains(message)));
        }
    }
}
