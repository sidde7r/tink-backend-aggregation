package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUserEligibilitiesItem {
    private String nonEligibilityReasonId;
    private EBankingUserId eBankingUserId;
    private String activity;
    private boolean status;

    public String getNonEligibilityReasonId() {
        return nonEligibilityReasonId;
    }

    public EBankingUserId getEBankingUserId() {
        return eBankingUserId;
    }

    public String getActivity() {
        return activity;
    }

    public boolean isStatus() {
        return status;
    }
}
