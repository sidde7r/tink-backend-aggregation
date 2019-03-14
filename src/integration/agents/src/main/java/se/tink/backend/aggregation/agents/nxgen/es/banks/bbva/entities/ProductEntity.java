package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.collection.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    private String name;
    private String description;
    private List<ComertialClassificationEntity> comertialClassifications;
    private String id;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ComertialClassificationEntity> getComertialClassifications() {
        return comertialClassifications;
    }

    public String getId() {
        return id;
    }
}
