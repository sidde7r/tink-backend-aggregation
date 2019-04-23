package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentificationDocumentEntity {

    @JsonProperty("codigodocumpersonacorp")
    private String documentNumber;

    @JsonProperty("tipodocumpersonacorp")
    private String documentType;

    String getDocumentNumber() {
        return documentNumber;
    }

    String getDocumentType() {
        return documentType;
    }
}
