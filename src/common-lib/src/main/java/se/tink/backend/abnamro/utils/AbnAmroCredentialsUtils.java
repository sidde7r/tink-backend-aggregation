package se.tink.backend.abnamro.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;

public class AbnAmroCredentialsUtils {

    /**
     * Filter the list and return the credentials that are getting data from aggregation
     */
    public static List<Credentials> getAggregationCredentials(List<Credentials> credentials) {
        return credentials.stream().filter(AbnAmroUtils::isAggregationCredentials).collect(Collectors.toList());
    }

    /**
     * Return true if the credential is eligible for receiving new transactions. Only credentials that are `UPDATING`
     * are eligible for historical pages. This logic is something that we should move away from since it makes it
     * impossible for ABN AMRO to trigger resyncs.
     */
    public static boolean isEligibleForHistoryTransactions(Credentials credentials) {
        return Objects.equals(credentials.getStatus(), CredentialsStatus.UPDATING)
                && !AbnAmroUtils.Predicates.IS_BLOCKED.apply(credentials);
    }

    /**
     * Return true if the credential is eligible for receiving single new transactions.
     */
    public static boolean isEligibleForSingleTransactions(Credentials credentials) {
        return !AbnAmroUtils.Predicates.IS_BLOCKED.apply(credentials);
    }
}
