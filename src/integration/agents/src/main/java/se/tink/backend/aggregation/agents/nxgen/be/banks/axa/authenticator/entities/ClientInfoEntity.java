package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class ClientInfoEntity {
    private String aorgCd;
    private String appliedLimitAmount;
    private String asscId;
    private String creditCardRequestAllowed;
    private String customerFirstName;
    private String customerId;
    private String customerLastName;
    private String deliveryDocuments;
    private String hasHBContract;
    private String haslimitMax;
    private String limitDate;
    private String limitUsedAmount;
    private String numberOfUnreadDigitalDocuments;
    private String openInvestAccountAllowed;
    private String openPensionAccountAllowed;
    private String partyId;
    private String productOpeningAllowed;
    private String startAccount;

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }
}
