package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JoseHeaderEntity {
    private String alg;
    private String x5c;
    private JpkEntity jpk;

    public void setAlg(String alg) {
        this.alg = alg;
    }

    /** X.509 Certificate Chain Header parameter */
    public void setX5c(String x5c) {
        this.x5c = x5c;
    }

    public void setJpk(JpkEntity jpk) {
        this.jpk = jpk;
    }
}
