package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface SupplementalInformationWaiter {
    Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, String initiator, String market);
}
