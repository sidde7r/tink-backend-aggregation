package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class AuthorizeRequest {
    private String authenticationMethodId;
    private String clientID;
    private PsuDataEntity psuData;
    private String redirectUri;
    private String scope;

    public static class AuthorizeRequestBuilder {
        private PsuDataEntity psuData;

        public AuthorizeRequestBuilder bankId(String bankId) {
            this.psuData = PsuDataEntity.builder().bankId(bankId).build();
            return this;
        }

        public AuthorizeRequestBuilder personalID(String personalID) {
            this.psuData = PsuDataEntity.builder().personalID(personalID).build();
            return this;
        }
    }
}
