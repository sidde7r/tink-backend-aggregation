package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class JpkEntity {
    private String alg;
    private String mod;
    private String exp;

    public JpkEntity(final String alg, final String mod, final String exp) {
        this.alg = alg;
        this.mod = mod;
        this.exp = exp;
    }
}
