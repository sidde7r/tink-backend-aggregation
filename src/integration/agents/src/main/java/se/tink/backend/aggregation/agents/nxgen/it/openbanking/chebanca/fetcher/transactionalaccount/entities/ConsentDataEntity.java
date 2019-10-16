package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentDataEntity {
    private List<CategoryEntity> categories;

    @JsonCreator
    public ConsentDataEntity(@JsonProperty("categories") List<CategoryEntity> categories) {
        this.categories = categories;
    }
}
