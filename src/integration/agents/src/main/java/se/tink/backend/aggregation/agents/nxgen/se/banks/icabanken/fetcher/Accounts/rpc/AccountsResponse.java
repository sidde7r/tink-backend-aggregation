package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends BaseResponse<AccountsBodyEntity> {
}
