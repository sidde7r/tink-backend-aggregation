package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategoriesEntity {
    private List<BanksEntity> banks;
    private String id;
    private String label;

    public List<BanksEntity> getBanks() {
        return banks;
    }

    public boolean isCheckingAccounts() {
        return id.equals(BoursoramaConstants.AccountCategories.CHECKING_ACCOUNT_ID);
    }

    public boolean isSavingsAccounts() {
        return id.equals(BoursoramaConstants.AccountCategories.SAVINGS_ACCOUNT_ID);
    }
}
