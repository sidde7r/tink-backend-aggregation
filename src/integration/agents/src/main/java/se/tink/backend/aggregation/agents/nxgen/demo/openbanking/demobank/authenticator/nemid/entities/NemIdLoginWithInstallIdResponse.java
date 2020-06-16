package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginWithInstallIdResponse {

    private String cprNo;
    private String personalId;
    private String advisor;
    private String customerName;
    private boolean campaignAvailable;
    private String sessionToken;


    public String getCprNo() {
        return cprNo;
    }

    public String getPersonalId() {
        return personalId;
    }

    public String getAdvisor() {
        return advisor;
    }

    public String getCustomerName() {
        return customerName;
    }

    public boolean isCampaignAvailable() {
        return campaignAvailable;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
