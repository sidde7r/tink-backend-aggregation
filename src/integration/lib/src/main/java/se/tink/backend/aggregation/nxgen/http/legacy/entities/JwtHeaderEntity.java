package se.tink.backend.aggregation.nxgen.http.legacy.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
