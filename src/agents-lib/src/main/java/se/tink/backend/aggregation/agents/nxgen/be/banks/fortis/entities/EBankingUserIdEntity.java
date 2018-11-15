package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUserIdEntity {
    private String personId;
    private String smid;
    private String agreementId;
    private String mobileStatus;
}
