package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseAccountsResponse {

    private List<BaseAccountEntity> accounts;

    public List<BaseAccountEntity> getAccounts() {
        return accounts;
    }
}
