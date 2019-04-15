package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDocumentEntity {
    private TypeEntity type;
    private String number;

    public TypeEntity getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }
}
