package se.tink.backend.aggregation.agents.framework;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

/**
 * Helper class allowing you to pass custom command line arguments to tests. This has a couple of
 * advantages over modifying (typically empty) string constants: Editing string constants makes the
 * credentials show up in git diff, polluting the total diff and increasing the risk of accidentally
 * committing the modification. Also, tests do not need to be @Ignore'd. If a credential has not
 * been supplied, the test will automatically be skipped. To pass custom arguments to your test,
 * supply them as Bazel flags. In IntelliJ, that would be: Select Run/Debug configuration -> Edit
 * configurations... -> Bazel flags window. Keep in mind that that system properties live on beyond
 * the lifetime of one test execution.
 *
 * <p>Some arguments may contain spaces or non-ASCII characters. Due to a bug or misconfiguration in
 * Bazel, these cannot be properly parsed. As a workaround, you have the option of passing in the
 * arguments URL-encoded. So instead of this:
 *
 * <pre>
 * --jvmopt=-Dtink.NAME="Claes Månsson"
 * </pre>
 *
 * do this:
 *
 * <pre>
 * --jvmopt=-Dtink.NAME=Claes%20M%C3%A5nsson
 * --jvmopt=-Dtink.urlencoded
 * </pre>
 */
public final class ArgumentManager<ArgumentEnum extends Enum<ArgumentEnum> & ArgumentManagerEnum> {

    public enum UsernamePasswordArgumentEnum implements ArgumentManagerEnum {
        USERNAME,
        PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum LoadBeforeSaveAfterArgumentEnum implements ArgumentManagerEnum {
        LOAD_BEFORE,
        SAVE_AFTER;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum UsernameArgumentEnum implements ArgumentManagerEnum {
        USERNAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum PasswordArgumentEnum implements ArgumentManagerEnum {
        PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum SsnArgumentEnum implements ArgumentManagerEnum {
        SSN;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum BusinessIdArgumentEnum implements ArgumentManagerEnum {
        CPI;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum ToAccountFromAccountArgumentEnum implements ArgumentManagerEnum {
        TO_ACCOUNT,
        FROM_ACCOUNT;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum IbanArgumentEnum implements ArgumentManagerEnum {
        IBAN;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum PsuIdArgumentEnum implements ArgumentManagerEnum {
        PSU_ID(true),
        PSU_ID_TYPE(true);

        private final boolean optional;

        PsuIdArgumentEnum(boolean optional) {
            this.optional = optional;
        }

        PsuIdArgumentEnum() {
            optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    public enum UserDataArgumentEnum implements ArgumentManagerEnum {
        EMAIL;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public enum BankIDPasswordArgumentEnum implements ArgumentManagerEnum {
        BANKID_PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    public interface ArgumentManagerEnum {
        boolean isOptional();
    }

    private static final Logger logger = LoggerFactory.getLogger(ArgumentManager.class);

    private static final String ARG_PREFIX = "tink.";

    private static int skippedTestsCount = 0;
    private static final Collection<String> missingArguments = new HashSet<>();

    private final State state = new State();

    private final Collection<ArgumentEnum> arguments;

    private static class State {
        private boolean isBeforeExecuted = false;
        private boolean isUrlEncoded = false;

        private void setIsBeforeExecuted() {
            isBeforeExecuted = true;
        }

        private boolean getIsBeforeExecuted() {
            return isBeforeExecuted;
        }

        private void setIsUrlEncoded() {
            isUrlEncoded = true;
        }

        private boolean getIsUrlEncoded() {
            return isUrlEncoded;
        }
    }

    /**
     * Declare the names of your command line parameters from an enum, e.g. enum { USERNAME,
     * PASSWORD }. The reason they have to be specified here is so the class knows what arguments to
     * look for when deciding whether the test should be skipped.
     */
    public ArgumentManager(ArgumentEnum[] argumentList) {
        arguments = ImmutableList.copyOf(argumentList);

        if (Objects.nonNull(System.getProperty("tink.urlencoded"))) {
            state.setIsUrlEncoded();
        }
    }

    /** Call this method in your @Before */
    public void before() {
        state.setIsBeforeExecuted();

        // Run tests only if the listed parameters have been passed as arguments, otherwise skip
        for (final ArgumentEnum arg : arguments) {
            final String propertyName = ARG_PREFIX + arg;
            if (getProperty(propertyName) == null && !arg.isOptional()) {
                missingArguments.add(arg.name());
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

    private String getProperty(@Nonnull final String propertyName) {
        final String propertyValue = System.getProperty(propertyName);
        if (state.getIsUrlEncoded()) {
            try {
                return URLDecoder.decode(propertyValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        String.format("Could not URL-decode value: %s", propertyValue));
            }
        }
        return propertyValue;
    }

    /**
     * @param property The enum property, e.g PASSWORD
     * @return Property value of the property associated with propertyName, e.g. the actual password
     */
    public String get(final ArgumentEnum property) {
        final String className = this.getClass().getName();
        Preconditions.checkState(
                state.getIsBeforeExecuted(),
                String.format("%s::before was not called prior to %s::get", className, className));

        final String propertyName = "tink." + property.name();
        if (!arguments.contains(property)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Argument '%s' was never declared. You declared: %s",
                            property.name(), arguments));
        }
        return getProperty(propertyName);
    }
}
