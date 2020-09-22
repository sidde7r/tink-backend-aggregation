package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class StartOtpResponse {
    private Boolean success;
}
