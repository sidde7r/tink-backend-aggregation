package se.tink.backend.aggregation.nxgen.http.legacy.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtHeaderEntity {

    private String alg = "RS256";
    private String typ = "JWT";
    private String kid;

    public JwtHeaderEntity(String kid) {
        this.kid = kid;
    }

    public String getAlg() {
        return alg;
    }

    public String getTyp() {
        return typ;
    }

    public String getKid() {
        return kid;
    }
}
