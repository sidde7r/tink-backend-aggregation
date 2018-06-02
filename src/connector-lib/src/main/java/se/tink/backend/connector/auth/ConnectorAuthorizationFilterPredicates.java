package se.tink.backend.connector.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import se.tink.libraries.auth.AuthorizationHeaderPredicate;

/**
 * Takes a map with tokens by client, validates structure and creates AuthorizationHeaderPredicate
 * for each one of them
 */
public class ConnectorAuthorizationFilterPredicates {

    private Map<String, AuthorizationHeaderPredicate> map = Maps.newHashMap();

    @Inject
    public ConnectorAuthorizationFilterPredicates(@Named("clientTokens") Map<String, List<String>> tokensByClient) {
        Preconditions.checkNotNull(tokensByClient, "Client list is null");
        Preconditions.checkArgument(!tokensByClient.isEmpty(), "Client list is empty");

        for (String client : tokensByClient.keySet()) {

            addTokens(client, tokensByClient.get(client));
        }
    }

    private void addTokens(String client, List<String> tokens) {

        Preconditions.checkArgument(!tokens.isEmpty(), "Client does not have any tokens");

        map.put(client, new AuthorizationHeaderPredicate("token", Predicates.in(ImmutableSet.copyOf(tokens))));
    }

    /**
     * Return the authorization header predicate for the specified client
     */
    public AuthorizationHeaderPredicate getPredicateByClient(String client) {

        Preconditions.checkArgument(map.containsKey(client), "Client not found");

        return map.get(client);
    }
}
