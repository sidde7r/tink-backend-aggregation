package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.BodyValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequest {
    private String country;
    private List<String> scope;
    private String state;
    private int duration;

    @JsonProperty("authentication_method")
    private String authenticationMethod;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("max_tx_history")
    private int maxTransactionHistory;

    @JsonProperty("account_list")
    private List<String> accountList = ImmutableList.of(BodyValues.ALL_WITH_CARDS);

    @JsonProperty("skip_account_selection")
    private boolean skipAccountSelection;

    private AuthorizeRequest(AuthorizeRequestBuilder builder) {
        this.country = builder.country;
        this.scope = builder.scope;
        this.state = builder.state;
        this.authenticationMethod = builder.authenticationMethod;
        this.redirectUri = builder.redirectUri;
        this.maxTransactionHistory = builder.maxTransactionHistory;
        this.duration = builder.duration;
        this.skipAccountSelection = BodyValues.SKIP_ACCOUNT_SELECTION;
    }

    public static class AuthorizeRequestBuilder {
        private String authenticationMethod;
        private String country;
        private String redirectUri;
        private List<String> scope;
        private String state;
        private int maxTransactionHistory;
        private int duration;

        public AuthorizeRequestBuilder withAuthenticationMethod(String authenticationMethod) {
            this.authenticationMethod = authenticationMethod;
            return this;
        }

        public AuthorizeRequestBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public AuthorizeRequestBuilder withRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public AuthorizeRequestBuilder withScope(List<String> scope) {
            this.scope = scope;
            return this;
        }

        public AuthorizeRequestBuilder withState(String state) {
            this.state = state;
            return this;
        }

        public AuthorizeRequestBuilder withMaxTransactionHistory(int maxTransactionHistory) {
            this.maxTransactionHistory = maxTransactionHistory;
            return this;
        }

        public AuthorizeRequestBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public AuthorizeRequest build() {
            return new AuthorizeRequest(this);
        }
    }
}
