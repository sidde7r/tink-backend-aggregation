package se.tink.backend.aggregation.agents.contexts;

import java.util.Optional;
import se.tink.libraries.customerinfo.CustomerInfo;

public interface IdentityDataCacher {

    /**
     * Caches {@code customerInfo}, making {@code customerInfo} retrievable via {@code
     * IdentityDataCacher::getCustomerInfo} after this method has been executed.
     *
     * @param customerInfo Customer identity data
     */
    void updateCustomerInfo(CustomerInfo customerInfo);

    /**
     * @return The customer identity data previously stored using {@code
     *     IdentityDataCacher::updateCustomerInfo}, or Optional.empty() if none exists
     */
    Optional<CustomerInfo> getCustomerInfo();
}
