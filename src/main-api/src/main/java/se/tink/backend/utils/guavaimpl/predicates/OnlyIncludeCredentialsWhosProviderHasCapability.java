package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;


public class OnlyIncludeCredentialsWhosProviderHasCapability implements Predicate<Credentials> {

    private final ImmutableMap<String, Provider> providersByName;
    private final Provider.Capability capability;

    public OnlyIncludeCredentialsWhosProviderHasCapability(final ImmutableMap<String, Provider> providersByName,
            final Provider.Capability capability) {

        this.providersByName = providersByName;
        this.capability = capability;
    }

    @Override
    public boolean apply(Credentials credentials) {
        if (!providersByName.containsKey(credentials.getProviderName())) {
            return false;
        }

        return providersByName.get(credentials.getProviderName()).getCapabilities().contains(capability);
    }
}
