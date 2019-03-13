package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.control.Option;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RelatedContractEntity {
    private String id;
    private FormatsEntity formats;
    private TypeEntity relationType;

    public String getId() {
        return id;
    }

    public Option<FormatsEntity> getFormats() {
        return Option.of(formats);
    }

    public Option<TypeEntity> getRelationType() {
        return Option.of(relationType);
    }
}
