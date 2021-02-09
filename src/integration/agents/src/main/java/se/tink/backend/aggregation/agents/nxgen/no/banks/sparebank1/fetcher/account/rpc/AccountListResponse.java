package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountListResponse {
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts == null ? Collections.emptyList() : accounts;
    }
}
