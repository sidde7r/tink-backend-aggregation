package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse {
    @JsonProperty("masDatos")
    private boolean moreData;

    @JsonProperty("saldoTotal")
    private double totalBalance;

    @JsonProperty("cuenta")
    private List<AccountEntity> account;

    @JsonIgnore
    public List<TransactionalAccount> getTinkAccounts(HolderName holderName) {
        return Optional.ofNullable(account).orElse(Collections.emptyList()).stream()
                .map(account -> account.toTinkAccount(holderName))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public int getNumberOfAccounts() {
        return Optional.ofNullable(account).orElse(Collections.emptyList()).size();
    }

    public boolean isMoreData() {
        return moreData;
    }
}
