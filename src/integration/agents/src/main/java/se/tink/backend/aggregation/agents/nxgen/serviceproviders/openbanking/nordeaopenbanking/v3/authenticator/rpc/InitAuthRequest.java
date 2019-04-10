package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAuthRequest {
    @JsonProperty("response_type")
    private String responseType;

    @JsonProperty("psu_id")
    private String psuId;

    private List<String> scope;
    private String language;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("account_list")
    private List<String> accountList = new ArrayList<>();

    private long duration;
    private String state;

    public static InitAuthRequest create(String redirectUri, String ssn, String state) {
        InitAuthRequest req = new InitAuthRequest();
        req.responseType = NordeaBaseConstants.Authorization.RESPONSE_TYPE;
        req.scope = NordeaBaseConstants.Authorization.SCOPES;
        req.duration = NordeaBaseConstants.Authorization.TOKEN_DURATION;
        req.language = NordeaBaseConstants.Authorization.LANGUAGE;

        req.psuId = ssn;
        req.redirectUri = redirectUri;
        req.state = state;

        return req;
    }
}
