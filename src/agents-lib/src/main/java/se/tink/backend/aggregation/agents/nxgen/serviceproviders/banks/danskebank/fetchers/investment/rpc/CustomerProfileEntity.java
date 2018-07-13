package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerProfileEntity {
    private String agentUserId;
    private String phoneNo;
    private String emailAddress;
    private String emailFormat;
    private boolean notificationsAgreement;
    private double smsFee;

    public String getAgentUserId() {
        return agentUserId;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailFormat() {
        return emailFormat;
    }

    public boolean isNotificationsAgreement() {
        return notificationsAgreement;
    }

    public double getSmsFee() {
        return smsFee;
    }
}
