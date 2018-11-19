package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUser {
    private EBankingUserId eBankingUserId;

    public EBankingUserId getEBankingUserId() {
        return eBankingUserId;
    }
}
