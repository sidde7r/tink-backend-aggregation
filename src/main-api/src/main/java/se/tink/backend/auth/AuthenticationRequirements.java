package se.tink.backend.auth;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import se.tink.backend.core.enums.FeatureFlags;

public class AuthenticationRequirements {
    private final boolean authenticationRequired;
    private final boolean authorizedDeviceRequired;
    private final ImmutableSet<FeatureFlags.FeatureFlagGroup> requiredFeatureGroups;
    private final ImmutableSet<String> scopes;

    private AuthenticationRequirements(boolean authenticationRequired, boolean authorizedDeviceRequired,
            ImmutableSet<FeatureFlags.FeatureFlagGroup> requiredFeatureGroups, ImmutableSet<String> scopes) {
        this.authenticationRequired = authenticationRequired;
        this.authorizedDeviceRequired = authorizedDeviceRequired;
        this.requiredFeatureGroups = requiredFeatureGroups;
        this.scopes = scopes;
    }

    public static AuthenticationRequirements fromAuthenticated(Authenticated authenticated) {
        return new AuthenticationRequirements(authenticated.required(), authenticated.requireAuthorizedDevice(),
                ImmutableSet.copyOf(authenticated.requireFeatureGroup()),
                ImmutableSet.copyOf(authenticated.scopes()));
    }

    public static AuthenticationRequirements defaultRequirements() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public boolean isAuthorizedDeviceRequired() {
        return authorizedDeviceRequired;
    }

    public Set<FeatureFlags.FeatureFlagGroup> getRequiredFeatureGroups() {
        return requiredFeatureGroups;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public static class Builder {
        private boolean authenticationRequired = true;
        private boolean authorizedDeviceRequired = true;
        private Set<FeatureFlags.FeatureFlagGroup> requiredFeatureGroups = Sets.newHashSet();
        private Set<String> scopes = Sets.newHashSet();

        public AuthenticationRequirements build() {
            return new AuthenticationRequirements(authenticationRequired, authorizedDeviceRequired,
                    ImmutableSet.copyOf(requiredFeatureGroups), ImmutableSet.copyOf(scopes));
        }

        public Builder setAuthenticationRequired(boolean authenticationRequired) {
            this.authenticationRequired = authenticationRequired;
            return this;
        }

        public Builder setAuthorizedDeviceRequired(boolean authorizedDeviceRequired) {
            this.authorizedDeviceRequired = authorizedDeviceRequired;
            return this;
        }

        public Builder addRequireFeatureGroups(Collection<FeatureFlags.FeatureFlagGroup> requireFeatureGroups) {
            this.requiredFeatureGroups.addAll(requireFeatureGroups);
            return this;
        }

        public Builder addRequireFeatureGroup(FeatureFlags.FeatureFlagGroup requireFeatureGroup) {
            this.requiredFeatureGroups.add(requireFeatureGroup);
            return this;
        }

        public Builder addScope(String scope) {
            this.scopes.add(scope);
            return this;
        }

        public Builder addScopes(Collection<String> scopes) {
            this.scopes.addAll(scopes);
            return this;
        }
    }
}
