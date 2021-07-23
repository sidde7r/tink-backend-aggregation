package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class NameAddressEntity {
    private List<String> value;

    public String getAdditionalTransactionDescription() {
        return value.get(0);
    }
}
