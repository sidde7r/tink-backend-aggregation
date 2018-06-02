package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

/**
 * An RxJava {@link User} filter based on system properties. Useful when writing commands that traverse all our users.
 */
public class CommandLineUserIdFilter {
    private static final LogUtils log = new LogUtils(CommandLineUserIdFilter.class);

    public Predicate<String> constructUserIdFilterPredicate(Optional<String> userIdFilename, Optional<String> userIdPrefix,
            Optional<String> userIdStart) throws IOException {
        Predicate<String> userFilter = Predicates.alwaysTrue(); // Passthrough.

        if (userIdFilename.isPresent()) {
            File userIdFilterFile = new File(userIdFilename.get());
            log.info(String.format("Using '%s' file for user filtering.", userIdFilterFile.getAbsolutePath()));
            ImmutableSet<String> userIds = ImmutableSet.copyOf(Files.readLines(userIdFilterFile, Charsets.UTF_8));

            userFilter = Predicates.in(userIds);
        }

        if (userIdPrefix.isPresent()) {
            String userPrefix = userIdPrefix.get();
            Predicate<String> startsWithPredicate = se.tink.backend.utils.guavaimpl.Predicates.startsWith(userPrefix);

            userFilter = Predicates.and(userFilter, startsWithPredicate);
        }

        if (userIdStart.isPresent()) {
            final String smallestId = userIdStart.get();
            Predicate<String> startsWithPredicate = userId -> smallestId.compareTo(userId) < 1;

            userFilter = Predicates.and(userFilter, startsWithPredicate);
        }

        return userFilter;
    }
}
