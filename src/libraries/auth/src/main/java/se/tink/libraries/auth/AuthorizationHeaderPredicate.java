package se.tink.libraries.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class AuthorizationHeaderPredicate implements Predicate<String> {
    private static final Splitter SPLITTER =
            Splitter.on(" ").limit(2).omitEmptyStrings().trimResults();

    private String method;
    private Predicate<String> payloadPredicate;

    public AuthorizationHeaderPredicate(String method, Predicate<String> payloadPredicate) {

        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(method), "Authorization method needs to be specified");

        this.method = method;
        this.payloadPredicate = payloadPredicate;
    }

    @Override
    public boolean apply(String authorizationHeader) {
        ImmutableList<String> pieces = ImmutableList.copyOf(SPLITTER.split(authorizationHeader));

        if (pieces.size() != 2) {
            return false;
        }

        String authMethod = pieces.get(0);
        String authPayload = pieces.get(1);

        return method.equalsIgnoreCase(authMethod) && payloadPredicate.apply(authPayload);
    }
}
