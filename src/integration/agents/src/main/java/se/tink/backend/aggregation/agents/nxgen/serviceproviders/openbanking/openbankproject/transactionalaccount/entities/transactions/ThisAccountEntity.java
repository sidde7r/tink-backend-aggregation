package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ThisAccountEntity {

    private String id;
    private BankRoutingEntity bankRouting;
    private List<AccountRoutingEntity> accountRoutings;
    private List<HolderEntity> holders;

    public String getId() {
        return id;
    }

    public BankRoutingEntity getBankRouting() {
        return bankRouting;
    }

    public List<AccountRoutingEntity> getAccountRoutings() {
        return accountRoutings;
    }

    public List<HolderEntity> getHolders() {
        return holders;
    }
}
