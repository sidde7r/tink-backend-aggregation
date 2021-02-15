package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountsResponse {
    private List<AccountEntity> accounts;
}
