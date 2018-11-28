package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUserEligibilitiesEntity {
    private EBankingUserIdEntity eBankingUserId;
    private String activity;
    private boolean status;
    private String nonEligibilityReasonId;
}
