package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class NameAddressEntity {
    private List<String> value;

    @JsonIgnore
    public String getAdditionalTransactionDescription() {
        return value.get(0);
    }
}
