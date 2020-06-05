package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SupplementaryDataEntity {
    private List<String> acceptedAuthenticationApproach;
    private String successfulReportUrl;
    private String unsuccessfulReportUrl;

    public SupplementaryDataEntity(String redirectUrl) {
        acceptedAuthenticationApproach = Collections.singletonList("REDIRECT");
        successfulReportUrl = redirectUrl;
        unsuccessfulReportUrl = redirectUrl;
    }
}
