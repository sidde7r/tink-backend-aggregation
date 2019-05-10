package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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

    public String getBankId() {
        return bankId;
    }

    public boolean isAuthMethodExtendedUsage() {
        return authMethodExtendedUsage;
    }

    public String getChosenProfile() {
        return chosenProfile;
    }

    public String getIdentifiedUserSsn() {
        return identifiedUserSsn;
    }

    public String getIdentifiedUserName() {
        return identifiedUserName;
    }
}
