package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.LinkListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponseEntity {
    private List<AccountEntity> accounts;

    @JsonProperty("_links")
    private LinkListEntity links;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
