package se.tink.libraries.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;

public class ApiTokenAuthorizationHeaderPredicate implements Predicate<String> {

    private AuthorizationHeaderPredicate delegate;

    public ApiTokenAuthorizationHeaderPredicate(Collection<String> tokens) {
        Preconditions.checkArgument(
                !tokens.isEmpty(),
                "It doesn't make sense to have an empty list of allowed tokens.");

        delegate =
                new AuthorizationHeaderPredicate(
                        "token", Predicates.in(ImmutableSet.copyOf(tokens)));
    }

    @Override
    public boolean apply(String authorizationHeader) {
        return delegate.apply(authorizationHeader);
    }
}
