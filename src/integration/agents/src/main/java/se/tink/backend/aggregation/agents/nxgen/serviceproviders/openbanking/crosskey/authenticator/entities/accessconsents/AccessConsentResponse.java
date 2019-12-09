package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccessConsentResponse {

    private ResponseDataEntity data;
    private LinksEntity links;
    private MetaEntity meta;
    private RiskEntity risk;

    public ResponseDataEntity getData() {
        return data;
    }
}
