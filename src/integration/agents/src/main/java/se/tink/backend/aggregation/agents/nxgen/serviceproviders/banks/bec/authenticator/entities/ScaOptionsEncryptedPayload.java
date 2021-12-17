package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ScaOptionsEncryptedPayload {

    @JsonProperty private List<String> secondFactorOptions;

    public List<String> getSecondFactorOptions() {
        return secondFactorOptions;
    }
}
