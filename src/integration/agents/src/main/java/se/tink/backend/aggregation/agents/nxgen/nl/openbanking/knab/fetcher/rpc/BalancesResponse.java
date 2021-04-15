package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BalancesResponse {
    private AccountEntity account;
    private List<BalanceEntity> balances;
}
