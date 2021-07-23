package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NameAddressEntity {
    private List<String> value;

    public String getOwnerName() {
        return value.get(0);
    }
}
