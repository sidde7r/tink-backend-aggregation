package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ChallengeParametersEntity {
    @JsonProperty("otpSubject")
    private String otpNumber;
}
