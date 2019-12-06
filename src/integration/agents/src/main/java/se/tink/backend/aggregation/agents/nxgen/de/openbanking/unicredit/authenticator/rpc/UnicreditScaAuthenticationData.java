package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditScaAuthenticationData {

    private String scaAuthenticationData;

    public UnicreditScaAuthenticationData(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
    }
}
