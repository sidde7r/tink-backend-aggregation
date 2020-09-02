package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.AccountsAndIdentitiesResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountsAndIdentitiesResponse {
    private AccountsAndIdentitiesResponsePayload payload;
}
