package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse extends BaseResponse<TransactionsBodyEntity> {
}
