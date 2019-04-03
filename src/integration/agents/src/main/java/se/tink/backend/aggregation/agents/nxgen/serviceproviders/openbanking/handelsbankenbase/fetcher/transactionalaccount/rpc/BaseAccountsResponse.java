package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseAccountsResponse<T extends BaseAccountEntity> {

    private List<T> accounts;

    public List<T> getAccounts() {
        return accounts;
    }
}
