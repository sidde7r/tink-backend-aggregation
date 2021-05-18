package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.CategoryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class ConsentDataEntity {
    private List<CategoryEntity> categories;
}
