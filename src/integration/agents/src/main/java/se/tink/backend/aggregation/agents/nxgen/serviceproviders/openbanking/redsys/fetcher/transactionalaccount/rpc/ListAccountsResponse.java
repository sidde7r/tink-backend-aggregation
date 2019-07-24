package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListAccountsResponse {
    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;
    @JsonProperty private List<AccountEntity> accounts;

    @JsonIgnore
    public boolean hasBalances() {
        return accounts.stream().allMatch(AccountEntity::hasBalances);
    }

    @JsonIgnore
    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
