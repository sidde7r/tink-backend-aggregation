package se.tink.backend.core.oauth2;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import java.util.Set;

public class OAuth2ClientScopes {

    private static final Splitter SCOPE_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private Set<String> scopes;

    public OAuth2ClientScopes(String clientScope) {
        Preconditions.checkNotNull(clientScope);
        this.scopes = Sets.newHashSet(SCOPE_SPLITTER.split(clientScope));
    }

    public boolean isRequestedScopeValid(String requestedScopesString) {
        Preconditions.checkNotNull(requestedScopesString);
        Set<String> requestedScopes = Sets.newHashSet(SCOPE_SPLITTER.split(requestedScopesString));

        if (requestedScopes.isEmpty()) {
            // empty input or input containing only SCOPE_SPLITTER is not valid
            return false;
        }

        for (String requestedScope : requestedScopes) {
            if (!this.scopes.contains(requestedScope)) {
                return false;
            }
        }

        return true;
    }

    public Set<String> getScopeSet() {
        return scopes;
    }
}
