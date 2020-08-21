package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.AccountsResponsePayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse extends BaseResponse {
    private AccountsResponsePayload payload;
}
