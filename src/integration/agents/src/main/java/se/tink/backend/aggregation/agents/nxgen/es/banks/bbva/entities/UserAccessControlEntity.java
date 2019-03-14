package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.collection.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserAccessControlEntity {
    private List<IsVisibleEntity> isVisible;
    private List<IsManagedEntity> isManaged;

    public List<IsVisibleEntity> getIsVisible() {
        return isVisible;
    }

    public List<IsManagedEntity> getIsManaged() {
        return isManaged;
    }
}
