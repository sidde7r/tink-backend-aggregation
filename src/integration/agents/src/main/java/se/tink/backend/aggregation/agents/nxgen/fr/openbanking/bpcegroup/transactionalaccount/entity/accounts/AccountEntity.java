package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountEntity {

    private String cashAccountType;
    private AccountId accountId;
    private String resourceId;
    private String product;

    private String usage;
    private String psuStatus;
    private String name;
    private String bicFi;
    private String currency;
    private String details;
    private String linkedAccount;

    public boolean isTransactionalAccount() {
        return AccountType.TRANSACTIONAL.getType().equalsIgnoreCase(cashAccountType);
    }

    public String getIban() {
        return getAccountId().getIban();
    }
}
