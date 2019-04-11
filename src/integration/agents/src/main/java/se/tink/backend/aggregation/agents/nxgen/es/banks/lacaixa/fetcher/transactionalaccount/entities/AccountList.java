package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountList {

    @JsonProperty("cuentas")
    private List<AccountEntity> accounts;

    @JsonProperty("masDatos")
    private boolean moreData;

    @JsonIgnore
    public Collection<AccountEntity> getAccounts() {
        return accounts;
    }

    public boolean isEmpty() {
        return accounts.isEmpty();
    }
}
