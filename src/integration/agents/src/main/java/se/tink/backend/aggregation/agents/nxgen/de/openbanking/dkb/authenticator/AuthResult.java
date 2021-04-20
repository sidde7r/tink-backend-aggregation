package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static com.google.common.collect.Sets.union;
import static java.lang.Long.MAX_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Data
@JsonObject
public class AuthResult {
    private static final String PROMPT_FOR_AUTH_METHOD_SELECTION =
            "PROMPT_FOR_AUTH_METHOD_SELECTION";
    private static final Set<String> RETURN_CODES_POSITIVE = ImmutableSet.of("CORRECT");
    private static final Set<String> RETURN_CODES_NEGATIVE = ImmutableSet.of("FAILED");

    private static final Set<String> RETURN_CODES_FINAL =
            union(RETURN_CODES_NEGATIVE, RETURN_CODES_POSITIVE);

    private static final Set<String> METHODS_EXCLUDED_FROM_SELECTION = ImmutableSet.of("CHIPTAN");
    private static final Predicate<AuthMethod> IS_SELECTABLE =
            authMethod -> !METHODS_EXCLUDED_FROM_SELECTION.contains(authMethod.authenticationType);

    private String returnCode;
    private String actionCode;
    private String accessToken;
    private String authTypeSelected;
    private String challenge;

    @JsonProperty("authSelectionList")
    private List<AuthMethod> authMethods = new ArrayList<>();

    public boolean isAuthenticated() {
        return RETURN_CODES_POSITIVE.contains(returnCode);
    }

    boolean isAuthenticationFinished() {
        return RETURN_CODES_FINAL.contains(returnCode);
    }

    boolean isAuthMethodSelectionRequired() {
        return PROMPT_FOR_AUTH_METHOD_SELECTION.equals(actionCode)
                && !getSelectableAuthMethods().isEmpty();
    }

    List<AuthMethod> getSelectableAuthMethods() {
        return authMethods.stream().filter(IS_SELECTABLE).collect(toList());
    }

    OAuth2Token toOAuth2Token() {
        return OAuth2Token.createBearer(accessToken, null, MAX_VALUE);
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class AuthMethod implements SelectableMethod {
        @JsonProperty("authOptionId")
        private String identifier;

        @JsonProperty("authType")
        private String authenticationType;

        private String authInfo;

        public String getName() {
            return format("%s - %s", authenticationType, authInfo);
        }
    }
}
