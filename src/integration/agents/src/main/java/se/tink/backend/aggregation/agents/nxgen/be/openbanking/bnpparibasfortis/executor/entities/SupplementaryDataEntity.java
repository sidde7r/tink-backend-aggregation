package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.PaymentRequestValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SupplementaryDataEntity {
    private List<String> acceptedAuthenticationApproach;
    private String successfulReportUrl;
    private String unsuccessfulReportUrl;

    public SupplementaryDataEntity(String redirectUrl) {
        acceptedAuthenticationApproach = Arrays.asList(PaymentRequestValues.AUTHENTICATION_METHODS);
        successfulReportUrl = redirectUrl;
        unsuccessfulReportUrl = redirectUrl;
    }
}
