package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RelatedContractEntity {
    private String id;
    private FormatsEntity formats;
    private TypeEntity relationType;

    public String getId() {
        return id;
    }

    public Optional<FormatsEntity> getFormats() {
        return Optional.ofNullable(formats);
    }

    public Optional<TypeEntity> getRelationType() {
        return Optional.ofNullable(relationType);
    }
}
