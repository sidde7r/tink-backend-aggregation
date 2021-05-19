package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@NoArgsConstructor
public class LaBanquePostaleSupplementaryDataEntity {
    private static final String UNSUCCESSFUL_QUERY = "&error=authentication_error";
    private List<String> acceptedAuthenticationApproach;
    private String successfulReportUrl;
    private String unsuccessfulReportUrl;

    public LaBanquePostaleSupplementaryDataEntity(String redirectUrl) {
        acceptedAuthenticationApproach = Collections.singletonList("REDIRECT");
        successfulReportUrl = redirectUrl;
        unsuccessfulReportUrl = redirectUrl + UNSUCCESSFUL_QUERY;
    }
}
