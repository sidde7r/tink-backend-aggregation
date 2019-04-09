package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyAccountsEntity {
    private List<GroupEntity> groups;
    private String selectedAccount;

    public List<GroupEntity> getGroups() {
        return groups != null ? groups : Collections.emptyList();
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }
}
