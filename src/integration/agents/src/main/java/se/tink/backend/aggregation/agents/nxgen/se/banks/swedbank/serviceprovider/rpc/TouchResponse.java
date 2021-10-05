package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TouchResponse {
    private ClientEntity client;
    private String bankId;
    private String authenticationRole;
    private String authMethodName;
    private String authMethodDescription;
    private boolean authMethodExtendedUsage;
    private String identifiedUserName;
    private String bankName;
    private String chosenProfile;
    private String chosenProfileName;
    private String chosenProfileLanguage;
    private String formattedServerTime;
    private String serverTime;

    @JsonProperty("identifiedUser")
    private String identifiedUserSsn;
}
