package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import se.tink.backend.utils.LogUtils;

public class CommandLineUsernameFilter {
    private static final LogUtils log = new LogUtils(CommandLineUsernameFilter.class);

    public Optional<Predicate<String>> constructUsernameFilter(Optional<String> usernameFilename, Optional<String> username) throws IOException {
        if (username.isPresent()) {
            return Optional.of(Predicates.equalTo(username.get()));
        }

        if (usernameFilename.isPresent()) {
            File usernameFilterFile = new File(usernameFilename.get());
            log.info(String.format("Using '%s' file for user filtering.", usernameFilterFile.getAbsolutePath()));
            ImmutableSet<String> usernames = ImmutableSet.copyOf(Files.readLines(usernameFilterFile, Charsets.UTF_8));
            return Optional.of(Predicates.in(usernames));
        }
        return Optional.empty();
    }
}
