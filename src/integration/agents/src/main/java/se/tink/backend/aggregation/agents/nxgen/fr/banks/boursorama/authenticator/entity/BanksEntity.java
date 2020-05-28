package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BanksEntity {
    private List<AccountsEntity> accounts;
    private String id;
    private String label;

    public List<AccountsEntity> getAccounts() {
        return accounts;
    }
}
