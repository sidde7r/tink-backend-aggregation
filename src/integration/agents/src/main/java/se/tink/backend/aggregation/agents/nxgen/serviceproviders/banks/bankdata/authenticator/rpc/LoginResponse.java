package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private String cprNo;
    private String personalId;
    private String customerName;
    private boolean campaignAvailable;

    public String getCprNo() {
        return cprNo;
    }

    public String getPersonalId() {
        return personalId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public boolean isCampaignAvailable() {
        return campaignAvailable;
    }
}
