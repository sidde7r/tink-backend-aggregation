package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.AccountEntity;

public class ListAccountsResponse {

    @JsonProperty("cuentas")
    private List<AccountEntity> accounts;

    @JsonProperty("masDatos")
    private boolean moreData;

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList);
    }

    public boolean isMoreData() {
        return moreData;
    }
}
