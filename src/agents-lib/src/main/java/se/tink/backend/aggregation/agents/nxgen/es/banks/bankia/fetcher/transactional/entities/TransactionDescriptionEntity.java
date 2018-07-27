package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDescriptionEntity {
    @JsonProperty("identificadorConcepto")
    private String identifierConcept;
    @JsonProperty("descripcionConcepto")
    private String descriptionConcept;

    public String getIdentifierConcept() {
        return identifierConcept;
    }

    public String getDescriptionConcept() {
        return descriptionConcept;
    }
}
