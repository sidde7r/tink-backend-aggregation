package se.tink.backend.aggregation.agents.framework;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assume.assumeNotNull;

/**
 * Helper class allowing you to pass custom command line arguments to tests.
 * To pass custom arguments to your test, supply them as Bazel flags like so:
 * --jvmopt=-Dtink.username=myusername
 * --jvmopt=-Dtink.password=mypasswords
 * In IntelliJ, that would be:
 * Select Run/Debug configuration -> Edit configurations... -> Bazel flags window
 * Note that system properties live on beyond the lifetime of one test execution.
 */
public final class ArgumentHelper {
    private static final Logger logger = LoggerFactory.getLogger(ArgumentHelper.class);

    private static int skippedTestsCount = 0;

    private ImmutableList<String> arguments;

    /**
     * Pass the names of your command line parameters, e.g. "tink.username", "tink.password"
     */
    public ArgumentHelper(final String... argumentList) {
        arguments = ImmutableList.copyOf(argumentList);
    }

    /**
     * Call this method in your @Before
     */
    public void before() {
        // Run tests only if the listed parameters have been passed as arguments, otherwise skip
        skippedTestsCount++;
        for (final String arg : arguments) {
            assumeNotNull(System.getProperty(arg));
        }
        // This statement is unreachable if not every command line parameter has been assigned a value
        skippedTestsCount--;
    }

    /**
     * Call this method in your @AfterClass
     */
    public static void afterClass() {
        if (skippedTestsCount > 0) {
            logger.warn(String.format("Skipped %s tests", skippedTestsCount));
        }
    }

    /**
     * Get property value by property name
     */
    public String get(final String propertyName) {
        if (!arguments.contains(propertyName)) {
            throw new IllegalArgumentException(String.format("Argument '%s' was never registered", propertyName));
        }
        return System.getProperty(propertyName);
    }

    /**
     * Get property value by position in argument list
     */
    public String get(final int propertyIndex) {
        return System.getProperty(arguments.get(propertyIndex));
    }
}

