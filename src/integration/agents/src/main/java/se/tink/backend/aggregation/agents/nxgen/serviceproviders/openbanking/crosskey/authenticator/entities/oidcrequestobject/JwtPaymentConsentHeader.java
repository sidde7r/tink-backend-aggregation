package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtPaymentConsentHeader {
    private Boolean b64;
    private String kid;
    private String alg;
    private List<String> crit;

    @JsonProperty("http://openbanking.org.uk/iat")
    private Long httpOpenbankingOrgUkIat;

    @JsonProperty("http://openbanking.org.uk/tan")
    private String httpOpenbankingOrgUkTan;

    @JsonProperty("http://openbanking.org.uk/iss")
    private String httpOpenbankingOrgUkIss;

    public JwtPaymentConsentHeader(
            Boolean b64,
            String kid,
            String alg,
            List<String> crit,
            Long httpOpenbankingOrgUkIat,
            String httpOpenbankingOrgUkTan,
            String httpOpenbankingOrgUkIss) {
        this.b64 = b64;
        this.kid = kid;
        this.alg = alg;
        this.crit = crit;
        this.httpOpenbankingOrgUkIat = httpOpenbankingOrgUkIat;
        this.httpOpenbankingOrgUkTan = httpOpenbankingOrgUkTan;
        this.httpOpenbankingOrgUkIss = httpOpenbankingOrgUkIss;
    }
}
