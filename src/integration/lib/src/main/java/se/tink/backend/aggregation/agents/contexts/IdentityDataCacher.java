package se.tink.backend.aggregation.agents.contexts;

import java.util.Optional;
import se.tink.libraries.identitydata.IdentityData;

public interface IdentityDataCacher {

    /**
     * Caches {@code customerInfo}, making {@code customerInfo} retrievable via {@code
     * IdentityDataCacher::getIdentityData} after this method has been executed.
     *
     * @param customerInfo Customer identity data
     */
    void updateIdentityData(IdentityData customerInfo);

    /**
     * @return The customer identity data previously stored using {@code
     *     IdentityDataCacher::updateIdentityData}, or Optional.empty() if none exists
     */
    Optional<IdentityData> getIdentityData();
}
