package se.tink.backend.main.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import se.tink.backend.common.utils.CredentialsPredicate;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.UserDevice;
import se.tink.backend.utils.guavaimpl.Orderings;

final public class BestGuessSwedishSSNHelper {

    /**
     * Best effort to guess a SSN from a users credentials, which we can use for pinning the new device for the user.
     * <p>
     * Of all non-deleted credentials with SSN-style username, take the first SSN-username based on prio:
     * 1. Credential we haven't tried yet on the device
     * 2. CreditSafe credential
     * 3. BankID credentials
     * 4. Most recently updated
     *
     * @param credentials Credentials that are valid to use for pinning (primarily a credential that is connected and is
     *                    considered "unsafe" to use, e.g. pinned agents).
     */
    public static Optional<String> getBestGuessSwedishSSN(UserDevice userDevice,
            List<Credentials> credentials) {

        // 1. (Optional) If we've tried the SSN before, down-prioritize it if user is stuck in auth chain
        //    This makes it possible to currently try two different SSN's for the user (some support cases on this case)
        final Optional<Comparator<Credentials>> previousSSNComparator = userDevice
                .getPayloadRaw(UserDevice.PayloadKey.SWEDISH_SSN)
                .map(t -> Orderings.credentialsByUsername(t).reversed());

        // 2. CreditSafe
        Comparator<Credentials> credentialPriorities;
        if (previousSSNComparator.isPresent()) {
            credentialPriorities = previousSSNComparator.get().thenComparing(Orderings.CREDENTIALS_BY_CREDITSAFE);
        } else {
            credentialPriorities = Orderings.CREDENTIALS_BY_CREDITSAFE;
        }

        // 3. & 4. BankID and most recently updated
        credentialPriorities = credentialPriorities.thenComparing(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY);

        return credentials.stream()
                // SSN credentials
                .filter(CredentialsPredicate.CREDENTIAL_HAS_SSN_USERNAME)
                .max(credentialPriorities)
                .map(t -> t.getField(Field.Key.USERNAME));
    }
}
