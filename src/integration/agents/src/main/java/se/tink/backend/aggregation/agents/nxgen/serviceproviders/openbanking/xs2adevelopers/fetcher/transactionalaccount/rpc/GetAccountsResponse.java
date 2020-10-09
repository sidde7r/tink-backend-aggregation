package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class GetAccountsResponse {
    private List<AccountEntity> accounts;
}
