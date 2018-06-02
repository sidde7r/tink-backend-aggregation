package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import se.tink.backend.utils.LogUtils;

public class CommandLineUserFilterFactory {
    private static final LogUtils log = new LogUtils(CommandLineUserFilterFactory.class);

    private static final String ALLOW_ALL_USERS_PROPERTY_NAME = "allowAllUsers";
    private static final String USERID_FILENAME_PROPERTY_NAME = "userIdsFile";
    private static final String USERNAME_FILENAME_PROPERTY_NAME = "usernamesFile";
    private static final String USERID_PREFIX = "userIdPrefix";
    private static final String START_FROM_USERID = "startFromUserId";
    private static final String USERNAME_PROPERTY = "username";
    private static final String SUBSET_SIZE_RATIO = "subsetSizeRatio";

    private static final boolean userIdPreconditions() {
      return System.getProperty(USERID_FILENAME_PROPERTY_NAME) != null ||
              System.getProperty(USERID_PREFIX) != null ||
              System.getProperty(START_FROM_USERID) != null;
    }

    private static final Boolean usernamePreconditions() {
        return System.getProperty(USERNAME_PROPERTY) != null ||
                System.getProperty(USERNAME_FILENAME_PROPERTY_NAME) != null;
    }

    public boolean checkSubsetSizeRatio() {
        // subsetSizeRatio should only be set when traversing all users,
        // not when using the userIds file or userId/username properties
        return System.getProperty(SUBSET_SIZE_RATIO) != null
                && System.getProperty(ALLOW_ALL_USERS_PROPERTY_NAME) != null;
    }

    private static final CommandLineUsernameFilter usernameFilter = new CommandLineUsernameFilter();
    private static final CommandLineUserIdFilter userIdFilter = new CommandLineUserIdFilter();

    public Function<String, Boolean> createUserFilter() throws IOException {
        Preconditions.checkArgument(Boolean.getBoolean(ALLOW_ALL_USERS_PROPERTY_NAME) || userIdPreconditions() || usernamePreconditions(),
                String.format("For explicitness, you must either set '%s' filename system property, '%s' filename system property, '%s' user ID system property, '%s' username system property, or '%s' system property to true.",
                        USERID_FILENAME_PROPERTY_NAME, USERNAME_FILENAME_PROPERTY_NAME, USERID_PREFIX, USERNAME_PROPERTY, ALLOW_ALL_USERS_PROPERTY_NAME));

        if (userIdPreconditions()) {
            Predicate<String> userIdPredicate = userIdFilter.constructUserIdFilterPredicate(
                    Optional.ofNullable(System.getProperty(USERID_FILENAME_PROPERTY_NAME)),
                    Optional.ofNullable(System.getProperty(USERID_PREFIX)),
                    Optional.ofNullable(System.getProperty(START_FROM_USERID)));
            return (input) -> userIdPredicate.apply(input);
        } else {
            Optional<Predicate<String>> usernamePredicate = usernameFilter.constructUsernameFilter(
                    Optional.ofNullable(System.getProperty(USERNAME_FILENAME_PROPERTY_NAME)),
                    Optional.ofNullable(System.getProperty(USERNAME_PROPERTY)));
            if (usernamePredicate.isPresent()){
                return (input) -> usernamePredicate.get().apply(input);
            } else {
                return (input) -> Predicates.alwaysTrue().apply(input);
            }
        }
    }
}
