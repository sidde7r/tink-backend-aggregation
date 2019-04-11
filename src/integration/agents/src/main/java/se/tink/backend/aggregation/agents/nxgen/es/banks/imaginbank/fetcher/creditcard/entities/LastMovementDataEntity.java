package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LastMovementDataEntity {

    private String lastMov;
    private String impLastMov;
    private String monedaLastMov;
}
