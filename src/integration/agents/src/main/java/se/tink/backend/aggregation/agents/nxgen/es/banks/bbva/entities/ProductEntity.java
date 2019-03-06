package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    private String name;
    private String description;
    private List<ComertialClassificationsItemEntity> comertialClassifications;
    private String id;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ComertialClassificationsItemEntity> getComertialClassifications() {
        return comertialClassifications;
    }

    public String getId() {
        return id;
    }
}
