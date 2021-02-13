package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequest {
    private String authenticationMethodId = RequestValues.MOBILE_ID;
    private String clientID;
    @JsonIgnore private PsuDataEntity psuData;
    private String redirectUri;
    private String scope = RequestValues.ALL_SCOPES;

    public AuthorizeRequest(String clientID, String redirectUri, String bankId) {
        this.clientID = clientID;
        this.redirectUri = redirectUri;
        psuData = new PsuDataEntity(bankId);
    }

    public AuthorizeRequest() {}
}
