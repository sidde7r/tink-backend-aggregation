package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DecoupledBusinessAuthenticationRequest {
    private String authenticationMethod = NordeaBaseConstants.BodyValuesSe.AUTHENTICATION_METHOD;
    private String companyId;
    private String country = NordeaBaseConstants.BodyValuesSe.COUNTRY;
    private String psuId;
    private String responseType = NordeaBaseConstants.BodyValuesSe.CODE;

    public DecoupledBusinessAuthenticationRequest(String psuId, String companyId) {
        this.psuId = psuId;
        this.companyId = companyId;
    }
}
