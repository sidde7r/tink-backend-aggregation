package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Saldo {

    @JsonProperty("DIVISA")
    private String dIVISA;

    @JsonProperty("IMPORTE")
    private double iMPORTE;
}
