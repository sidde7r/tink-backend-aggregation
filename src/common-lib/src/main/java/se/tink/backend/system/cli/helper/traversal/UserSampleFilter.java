package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Predicate;
import java.io.IOException;
import java.util.Random;
import rx.functions.Func1;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

/**
 * An RxJava {@link User} filter based on system properties. Useful when writing commands that traverse all our users.
 */
public class UserSampleFilter implements Func1<User, Boolean> {
    private static final LogUtils log = new LogUtils(UserSampleFilter.class);
    private static final Random RANDOM = new Random();

    private Predicate<User> userFilter;

    public UserSampleFilter(UserRepository userRepository) throws IOException {

        int sampleSize = Integer.getInteger("approxSampleSize", 1000);
        long userCount = userRepository.count();

        if (userCount == 0) {
            log.error("There are no users in the database. Exiting");
            throw new IOException("There are no users in the database. Exiting");
        }

        double probabilityToInclude = calculateProbabilityToInclude(sampleSize, userCount);

        log.info("Chosen approxSampleSize: " + sampleSize);
        log.info("User Count is: " + userCount);
        log.info(String.format("Using a probability of %.02f when streaming through users.", probabilityToInclude));

        this.userFilter = user -> RANDOM.nextDouble() < probabilityToInclude;
    }

    private double calculateProbabilityToInclude(int sampleSize, long userCount) {
        double prob = sampleSize / (double) userCount;
        // Clamp probability between [0, 1]
        if (prob < 0) {
            return 0;
        } else if (prob > 1) {
            return 1;
        } else {
            return prob;
        }
    }

    @Override
    public Boolean call(User user) {
        return userFilter.apply(user);
    }
}
