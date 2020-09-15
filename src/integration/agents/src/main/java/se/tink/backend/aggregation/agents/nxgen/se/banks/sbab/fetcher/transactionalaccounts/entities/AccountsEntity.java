package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsEntity extends StandardResponse {
    private List<SBABTransactionalAccountsEntity> personalAccounts = Collections.emptyList();
    private List<SBABTransactionalAccountsEntity> sharedAccounts = Collections.emptyList();
}
