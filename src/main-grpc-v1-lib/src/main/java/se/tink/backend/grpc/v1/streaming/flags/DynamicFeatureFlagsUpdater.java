package se.tink.backend.grpc.v1.streaming.flags;

import com.google.inject.Inject;
import java.util.List;
import java.util.function.Predicate;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.main.controllers.UserServiceController;

public class DynamicFeatureFlagsUpdater {
    private final UserServiceController userServiceController;
    private final ResidenceTabHelper residenceHelper;

    @Inject
    public DynamicFeatureFlagsUpdater(UserServiceController userServiceController, ResidenceTabHelper residenceHelper) {
        this.userServiceController = userServiceController;
        this.residenceHelper = residenceHelper;
    }

    public void updateResidenceTabFlag(User user, List<Account> allAccounts) {
        updateResidenceTabFlag(user, shouldDisplayResidenceTab(user, allAccounts));
    }

    private void updateResidenceTabFlag(User user, Predicate<ResidenceTabHelper> predicate) {
        List<String> flags = user.getFlags();
        flags.addAll(userServiceController.generateDynamicFlags(user));

        // Check if we should show the residence tab in the app.
        if (predicate.test(residenceHelper)) {
            if (!flags.contains(FeatureFlags.RESIDENCE_TAB)) {
                flags.add(FeatureFlags.RESIDENCE_TAB);
            }
        } else {
            // Remove the flag since we have a legacy logic for the old apps where it is dynamically inherited if the
            // user has the `APPLICATION` flag.
            flags.remove(FeatureFlags.RESIDENCE_TAB);
        }

        user.setFlags(flags);
    }

    private static Predicate<ResidenceTabHelper> shouldDisplayResidenceTab(User user, List<Account> accounts) {
        return residenceTabHelper -> residenceTabHelper.shouldDisplayResidenceTab(user, accounts);
    }
}
