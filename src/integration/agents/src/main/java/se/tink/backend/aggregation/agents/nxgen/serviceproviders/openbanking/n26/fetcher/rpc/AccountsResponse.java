package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse {

    private List<AccountEntity> accounts;
}
