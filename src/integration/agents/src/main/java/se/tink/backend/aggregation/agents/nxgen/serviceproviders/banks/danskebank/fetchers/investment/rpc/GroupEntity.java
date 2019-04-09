package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GroupEntity {
    private String name;
    private List<GroupAccountEntity> accounts;

    public String getName() {
        return name;
    }

    public List<GroupAccountEntity> getAccounts() {
        return accounts;
    }
}
