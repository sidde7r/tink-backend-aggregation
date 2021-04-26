package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.AccountType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model.Entity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    private List<Entity> entities;

    @JsonIgnore
    public List<Entity> getEntities(AccountType type) {
        return entities.stream()
                .filter(entity -> entity.hasType(type.getEntityType()))
                .collect(Collectors.toList());
    }
}
