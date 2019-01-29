package se.tink.libraries.abnamro.utils;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.abnamro.client.rpc.AuthenticatedRequest;

public class AbnAmroLegacyUserUtils {

    private static final String USERNAME_FORMAT = "abnamro-%s";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^abnamro-(?<value>\\d+)$");

    private static final String GRIP_LEGACY_DEEPLINK_PREFIX = "tink://";
    private static final String GRIP_DEEPLINK_PREFIX = "^grip://";

    // List of bc numbers that are test users on ABN side
    public static final ImmutableList<String> TEST_USERS_BC_NUMBERS = ImmutableList.of("105238465", "203339193");

    public static String getUsername(AuthenticatedRequest request) {
        return getUsername(request.getBcNumber());
    }

    public static String getUsername(String bcNumber) {
        return String.format(USERNAME_FORMAT, bcNumber);
    }

    public static boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isTestUser(String bcNumber) {
        return TEST_USERS_BC_NUMBERS.contains(bcNumber);
    }

    public static Optional<String> getBcNumber(User user) {

        Matcher matcher = USERNAME_PATTERN.matcher(user.getUsername());

        if (matcher.find()) {
            return Optional.of(matcher.group("value"));
        }

        return Optional.empty();
    }
}
