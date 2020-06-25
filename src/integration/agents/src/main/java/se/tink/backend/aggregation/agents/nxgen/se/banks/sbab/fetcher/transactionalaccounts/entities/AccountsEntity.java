package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity extends StandardResponse {
    private List<PersonalAccountsEntity> personalAccounts;

    public List<PersonalAccountsEntity> getPersonalAccounts() {
        return Optional.ofNullable(personalAccounts).orElse(Collections.emptyList());
    }
}
