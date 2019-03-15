package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.rpc.BaseAccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends BaseAccountsResponse<AccountEntity> {

}
