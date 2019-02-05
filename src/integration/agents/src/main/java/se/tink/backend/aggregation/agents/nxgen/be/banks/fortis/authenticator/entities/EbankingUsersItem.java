package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EbankingUsersItem {
    private EBankingUser eBankingUser;

    public EBankingUser getEBankingUser() {
        return eBankingUser;
    }
}
