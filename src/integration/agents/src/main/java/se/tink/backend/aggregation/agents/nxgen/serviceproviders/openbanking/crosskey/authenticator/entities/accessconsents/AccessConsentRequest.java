package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccessConsentRequest {

    private RequestDataEntity data;
    private RiskEntity risk;

    public AccessConsentRequest(RequestDataEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }
}
