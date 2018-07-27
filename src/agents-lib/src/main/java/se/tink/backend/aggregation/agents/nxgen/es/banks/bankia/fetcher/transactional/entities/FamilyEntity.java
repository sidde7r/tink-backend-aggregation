package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FamilyEntity {
    @JsonProperty("idFamilia")
    private String idFamily;
    @JsonProperty("idSubfamilia")
    private String idSubfamily;
}
