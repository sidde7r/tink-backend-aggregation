package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EbankingUsersValue {
    private String minor;
    private List<EBankingUserEligibilitiesItem> eBankingUserEligibilities;
    private List<EbankingUsersItem> eBankingUsers;

    public String getMinor() {
        return minor;
    }

    public List<EBankingUserEligibilitiesItem> getEBankingUserEligibilities() {
        return eBankingUserEligibilities;
    }

    public List<EbankingUsersItem> getEBankingUsers() {
        return eBankingUsers;
    }
}
