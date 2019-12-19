package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BasicEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NumberFormatsEntity {
    private String number;
    private BasicEntity numberType;

    public NumberFormatsEntity() {}

    public NumberFormatsEntity(String number, String typeId) {
        this.number = number;
        this.numberType = new BasicEntity(typeId);
    }
}
