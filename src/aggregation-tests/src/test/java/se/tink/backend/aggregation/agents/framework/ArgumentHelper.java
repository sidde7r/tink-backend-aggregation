package se.tink.backend.aggregation.agents.framework;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assume.assumeNotNull;

/**
 * Helper class allowing you to pass custom command line arguments to tests.
 * This has a couple of advantages over modifying (typically empty) string constants:
 * - Editing string constants makes the credentials show up in git diff, polluting the total diff and increasing the
 * risk of accidentally commiting the modifiction.
 * - Test do not need to be @Ignore'd. If a credential has not been supplied, the test will automatically be skipped.
 * To pass custom arguments to your test, supply them as Bazel flags like so:
 * --jvmopt=-Dtink.username=myusername
 * --jvmopt=-Dtink.password=mypassword
 * In IntelliJ, that would be:
 * Select Run/Debug configuration -> Edit configurations... -> Bazel flags window
 * Note that system properties live on beyond the lifetime of one test execution.
 */
public final class ArgumentHelper {
    private static final Logger logger = LoggerFactory.getLogger(ArgumentHelper.class);

    private static int skippedTestsCount = 0;

    private ImmutableList<String> arguments;

    /**
     * Declare the names of your command line parameters, e.g. "tink.username", "tink.password".
     * The reason they have to be specified here is so the class knows what arguments to look for when deciding whether
     * the test should be skipped.
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
     * Call this method in your @AfterClass. This will cause the helper to log a warning if any tests were skipped.
     */
    public static void afterClass() {
        if (skippedTestsCount > 0) {
            logger.warn(String.format("Skipped %s tests", skippedTestsCount));
        }
    }

    /**
     * @param propertyName The name of the property, e.g. "tink.password"
     * @return Property value of the property associated with propertyName, e.g. the actual password
     */
    public String get(final String propertyName) {
        if (!arguments.contains(propertyName)) {
            throw new IllegalArgumentException(String.format("Argument '%s' was never registered", propertyName));
        }
        return System.getProperty(propertyName);
    }

    /**
     * @param propertyIndex The index of the property whose value is accessed (zero-indexed), e.g. 1
     * @return Property value of the propertyIndex'th property, e.g. the actual password
     */
    public String get(final int propertyIndex) {
        return System.getProperty(arguments.get(propertyIndex));
    }
}

