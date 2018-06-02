package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import java.util.Map;
import se.tink.backend.core.Provider;
import se.tink.backend.core.product.ProductFilterRule;

public class ProviderCapabilityPredicate implements Predicate<Profile> {
    private final Provider.Capability capability;
    private final Map<String, Provider> providersByName;

    public ProviderCapabilityPredicate(ProductFilterRule rule, Map<String, Provider> providersByName) {
        String capabilityName = (String) rule.getPayload();
        this.capability = Provider.Capability.valueOf(capabilityName);
        this.providersByName = providersByName;
    }

    @Override
    public boolean apply(Profile profile) {

        for (String providerName : profile.getCredentials().keySet()) {
            Provider provider = providersByName.get(providerName);
            if (provider != null && provider.getCapabilities().contains(capability)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("capability", capability)
                .toString();
    }
}
