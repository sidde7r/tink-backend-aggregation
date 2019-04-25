package se.tink.backend.aggregation.agents.framework;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class allowing you to pass custom command line arguments to tests. This has a couple of
 * advantages over modifying (typically empty) string constants: Editing string constants makes the
 * credentials show up in git diff, polluting the total diff and increasing the risk of accidentally
 * committing the modification. Also, tests do not need to be @Ignore'd. If a credential has not
 * been supplied, the test will automatically be skipped. To pass custom arguments to your test,
 * supply them as Bazel flags. In IntelliJ, that would be: Select Run/Debug configuration -> Edit
 * configurations... -> Bazel flags window. Keep in mind that that system properties live on beyond
 * the lifetime of one test execution.
 */
public final class ArgumentManager<ArgumentEnum extends Enum<ArgumentEnum>> {
    private static final Logger logger = LoggerFactory.getLogger(ArgumentManager.class);

    private static final String ARG_PREFIX = "tink.";

    private static int skippedTestsCount = 0;
    private static final Collection<String> missingArguments = new HashSet<>();

    private class State {
        private boolean isBeforeExecuted = false;

        private void setIsBeforeExecuted() {
            isBeforeExecuted = true;
        }

        private boolean getIsBeforeExecuted() {
            return isBeforeExecuted;
        }
    }

    private final State state = new State();

    private final Collection<String> arguments;

    /**
     * Declare the names of your command line parameters from an enum, e.g. enum { USERNAME,
     * PASSWORD }. The reason they have to be specified here is so the class knows what arguments to
     * look for when deciding whether the test should be skipped.
     */
    public ArgumentManager(ArgumentEnum[] argumentList) {
        arguments =
                ImmutableList.copyOf(
                        Arrays.stream(argumentList).map(Enum::name).collect(Collectors.toList()));
    }

    /** Call this method in your @Before */
    public void before() {
        state.setIsBeforeExecuted();
        // Run tests only if the listed parameters have been passed as arguments, otherwise skip
        for (final String arg : arguments) {
            final String propertyName = ARG_PREFIX + arg;
            if (System.getProperty(propertyName) == null) {
                missingArguments.add(arg);
            }
        }
        if (!missingArguments.isEmpty()) {
            skippedTestsCount++;
            Assume.assumeTrue(false); // Will terminate the method here if the property is missing
        }
    }

    /**
     * Call this method in your @AfterClass. This will cause the class to log a warning if any tests
     * were skipped.
     */
    public static void afterClass() {
        if (skippedTestsCount > 0) {
            final List<String> missingArgs =
                    missingArguments.stream()
                            .map(arg -> ARG_PREFIX + arg)
                            .collect(Collectors.toList());
            final String header =
                    String.format(
                            "Skipped %d test(s) because arguments were not supplied: %s",
                            skippedTestsCount, missingArgs);
            final String explanation = "Please supply the arguments as Bazel flags like so:";
            final Function<String, String> argToFlag =
                    arg ->
                            String.format(
                                    "--jvmopt=-D%s%s=%s",
                                    ARG_PREFIX, arg, "my" + arg.toLowerCase());
            final List<String> lines =
                    missingArguments.stream().map(argToFlag).collect(Collectors.toList());
            logger.warn(String.format("%s\n%s\n%s", header, explanation, String.join("\n", lines)));
        }
    }

    /**
     * @param property The enum property, e.g PASSWORD
     * @return Property value of the property associated with propertyName, e.g. the actual password
     */
    public String get(@Nonnull final ArgumentEnum property) {
        Preconditions.checkNotNull(property);
        final String className = this.getClass().getName();
        Preconditions.checkState(
                state.getIsBeforeExecuted(),
                String.format("%s::before was not called prior to %s::get", className, className));

        final String propertyName = "tink." + property.name();
        if (!arguments.contains(property.name())) {
            throw new IllegalArgumentException(
                    String.format(
                            "Argument '%s' was never declared. You declared: %s",
                            property.name(), arguments));
        }
        return System.getProperty(propertyName);
    }
}
