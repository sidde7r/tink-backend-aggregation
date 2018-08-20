package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class CFRequest {
    private String paylIWV;
    private String z;
    private String x;

    public CFRequest(final String paylIWV, final String z, final String x) {
        this.paylIWV = paylIWV;
        this.z = z;
        this.x = x;
    }
}
