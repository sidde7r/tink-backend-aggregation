package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class AccountsResponse {

    private List<AccountsItemEntity> accounts;

    public List<AccountsItemEntity> getAccounts() {
        return accounts;
    }
}
