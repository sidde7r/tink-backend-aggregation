package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public abstract class BaseResponseEntity {
    private Boolean deleted;
    protected String id;
    protected long sort;
}
