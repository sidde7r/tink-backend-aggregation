package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalAccountsResponse extends SpankkiResponse {
    @JsonProperty private List<AccountEntity> accounts;

    @JsonIgnore
    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
