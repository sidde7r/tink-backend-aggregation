package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RelatedContractEntity {
    private FormatsEntity formats;

    public FormatsEntity getFormats() {
        return formats;
    }
}
