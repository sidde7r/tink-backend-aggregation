package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserAccessControlEntity {
    private List<IsVisibleItemEntity> isVisible;
    private List<IsManagedItemEntity> isManaged;

    public List<IsVisibleItemEntity> getIsVisible() {
        return isVisible;
    }

    public List<IsManagedItemEntity> getIsManaged() {
        return isManaged;
    }
}
