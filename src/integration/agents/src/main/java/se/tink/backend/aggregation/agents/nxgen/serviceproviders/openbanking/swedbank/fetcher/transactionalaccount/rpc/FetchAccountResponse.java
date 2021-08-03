package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountResponse {

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccountList() {
        return accounts;
    }

    public List<String> getIbanList() {
        return accounts.stream().map(AccountEntity::getIban).collect(Collectors.toList());
    }
}
