package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessEntity {
    private List<AccountDetailsEntity> balances;
    private List<AccountDetailsEntity> transactions;
    private String availableAccounts;
    private AccessType allPsd2;

    public AccessEntity(String availableAccounts) {
        this.availableAccounts = availableAccounts;
    }

    public AccessEntity(AccessType allPsd2) {
        this.allPsd2 = allPsd2;
    }

    public AccessEntity(
            List<AccountDetailsEntity> balances, List<AccountDetailsEntity> transactions) {
        this.balances = balances;
        this.transactions = transactions;
    }
}
