package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentJWTHeader {

    private String alg;
    private Boolean b64;
    private List<String> crit;

    @JsonProperty("http://openbanking.org.uk/iat")
    private Long httpOpenbankingOrgUkIat;

    @JsonProperty("http://openbanking.org.uk/iss")
    private String httpOpenbankingOrgUkIss;

    private String kid;

    public ConsentJWTHeader(
            String alg,
            Boolean b64,
            List<String> crit,
            Long httpOpenbankingOrgUkIat,
            String httpOpenbankingOrgUkIss,
            String kid) {
        this.alg = alg;
        this.b64 = b64;
        this.crit = crit;
        this.httpOpenbankingOrgUkIat = httpOpenbankingOrgUkIat;
        this.httpOpenbankingOrgUkIss = httpOpenbankingOrgUkIss;
        this.kid = kid;
    }
}
