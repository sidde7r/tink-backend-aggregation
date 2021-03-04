package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public abstract class BaseResponseEntity {
    private Boolean deleted;
    protected String id;
    @Setter protected long sort;

    @JsonIgnore
    public boolean notDeleted() {
        return BooleanUtils.isNotTrue(deleted);
    }
}
