package se.tink.backend.aggregation.agents.abnamro.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.user.rpc.User;

public class AbnAmroLegacyUserUtils {

    private static final String USERNAME_FORMAT = "abnamro-%s";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^abnamro-(?<value>\\d+)$");

    public static String getUsername(String bcNumber) {
        return String.format(USERNAME_FORMAT, bcNumber);
    }

    public static boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static Optional<String> getBcNumber(User user) {

        Matcher matcher = USERNAME_PATTERN.matcher(user.getUsername());

        if (matcher.find()) {
            return Optional.of(matcher.group("value"));
        }

        return Optional.empty();
    }
}
