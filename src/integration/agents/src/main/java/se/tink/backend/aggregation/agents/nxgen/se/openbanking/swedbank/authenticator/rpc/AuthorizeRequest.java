package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequest {
    private String authenticationMethodId = RequestValues.MOBILE_ID;
    private String clientID;
    private PsuDataEntity psuData;
    private String redirectUri;
    private String scope = RequestValues.PSD2;

    public AuthorizeRequest(String clientID, String redirectUri) {
        this.clientID = clientID;
        this.redirectUri = redirectUri;
        psuData = new PsuDataEntity();
    }
}
