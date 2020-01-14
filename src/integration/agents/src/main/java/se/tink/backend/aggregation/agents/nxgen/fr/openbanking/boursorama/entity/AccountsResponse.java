package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
