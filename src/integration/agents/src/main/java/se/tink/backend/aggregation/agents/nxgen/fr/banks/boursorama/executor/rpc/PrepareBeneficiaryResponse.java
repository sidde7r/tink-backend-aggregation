package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrepareBeneficiaryResponse {

    @JsonProperty("dataId")
    private String beneficiaryId;
}
