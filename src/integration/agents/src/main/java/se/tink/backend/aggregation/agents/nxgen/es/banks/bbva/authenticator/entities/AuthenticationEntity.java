package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AuthenticationEntity {
    private final String userID;
    private final String consumerID;
    private final String authenticationType;
    private List<AuthenticationDataEntity> authenticationData;
    private String authenticationState;
    private String multistepProcessId;

    public AuthenticationEntity(AuthenticationBuilder builder) {
        this.userID = builder.userID;
        this.consumerID = builder.consumerID;
        this.multistepProcessId = builder.multistepProcessId;
        this.authenticationData = builder.authenticationData;
        this.authenticationType = builder.authenticationType;
        this.authenticationState = builder.authenticationState;
    }

    public static AuthenticationBuilder builder() {
        return new AuthenticationBuilder();
    }

    public static class AuthenticationBuilder {
        private final String consumerID = LoginParameter.CONSUMER_ID;
        private String userID;
        private String authenticationType;
        private List<AuthenticationDataEntity> authenticationData;
        private String authenticationState;
        private String multistepProcessId;

        public AuthenticationBuilder withUserID(String userID) {
            this.userID = LoginParameter.USER_VALUE_PREFIX + userID;
            return this;
        }

        public AuthenticationBuilder withAuthenticationType(String authenticationType) {
            this.authenticationType = authenticationType;
            return this;
        }

        public AuthenticationBuilder withAuthenticationData(
                List<AuthenticationDataEntity> authenticationData) {
            this.authenticationData = authenticationData;
            return this;
        }

        public AuthenticationBuilder withAuthenticationState(String authenticationState) {
            this.authenticationState = authenticationState;
            return this;
        }

        public AuthenticationBuilder withMultistepProcessId(String multistepProcessId) {
            this.multistepProcessId = multistepProcessId;
            return this;
        }

        public AuthenticationEntity build() {
            return new AuthenticationEntity(this);
        }
    }
}
