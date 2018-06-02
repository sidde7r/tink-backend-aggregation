package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAccountGroupEntity {
    private String name;
    private List<TransferDestinationAccountEntity> accounts;

    public String getName() {
        return name;
    }

    public List<TransferDestinationAccountEntity> getAccounts() {
        return accounts;
    }
}
