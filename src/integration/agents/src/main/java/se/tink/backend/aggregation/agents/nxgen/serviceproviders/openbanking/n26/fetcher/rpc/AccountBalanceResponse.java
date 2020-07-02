package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.AccountBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountBalanceResponse {

    private AccountBalanceEntity balance;
}
