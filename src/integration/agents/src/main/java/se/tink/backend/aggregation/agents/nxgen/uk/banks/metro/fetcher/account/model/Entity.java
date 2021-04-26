package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class Entity {
    private String entityId;

    private String entityName;

    private EntityType entityType;

    private List<AccountEntity> accounts;

    @JsonIgnore
    public boolean hasType(EntityType entityType) {
        return this.entityType == entityType;
    }
}
