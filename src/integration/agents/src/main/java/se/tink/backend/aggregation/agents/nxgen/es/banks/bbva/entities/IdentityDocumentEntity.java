package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class IdentityDocumentEntity {
    private String number;
    private IdentityDocumentTypeEntity type;
}
