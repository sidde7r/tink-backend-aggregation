package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionReferenceEntity {
    @JsonProperty("codigoCampo")
    private String fieldCode;

    @JsonProperty("codigoPlantilla")
    private String templateCode;

    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("nombreCorto")
    private String shortName;

    @JsonProperty("longitudPlantilla")
    private String templateLength;
}
