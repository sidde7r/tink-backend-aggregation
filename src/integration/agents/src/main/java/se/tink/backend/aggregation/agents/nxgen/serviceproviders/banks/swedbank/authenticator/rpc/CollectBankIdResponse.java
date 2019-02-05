package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollectBankIdResponse extends AbstractBankIdAuthResponse {
    private boolean extendedUsage;
    private String formattedServerTime;
    private String serverTime;
    private String authenticationRole;

    public boolean isExtendedUsage() {
        return extendedUsage;
    }

    public String getFormattedServerTime() {
        return formattedServerTime;
    }

    public String getServerTime() {
        return serverTime;
    }

    public String getAuthenticationRole() {
        return authenticationRole;
    }
}
