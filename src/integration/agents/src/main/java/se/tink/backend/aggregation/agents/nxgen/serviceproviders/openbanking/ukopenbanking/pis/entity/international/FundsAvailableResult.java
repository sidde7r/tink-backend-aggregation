package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class FundsAvailableResult {
    private String fundsAvailableDateTime;
    private boolean fundsAvailable;

    public String getFundsAvailableDateTime() {
        return fundsAvailableDateTime;
    }

    public boolean isFundsAvailable() {
        return fundsAvailable;
    }
}
