package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TouchResponse {
    private ClientEntity client;
    private String bankId;
    private String authenticationRole;
    private String authMethodName;
    private String authMethodDescription;
    private boolean authMethodExtendedUsage;
    private String identifiedUser;
    private String identifiedUserName;
    private String bankName;
    private String chosenProfile;
    private String chosenProfileName;
    private String chosenProfileLanguage;
    private String formattedServerTime;
    private String serverTime;

    public String getBankId() {
        return bankId;
    }

    public boolean isAuthMethodExtendedUsage() {
        return authMethodExtendedUsage;
    }

    public String getChosenProfile() {
        return chosenProfile;
    }
}
