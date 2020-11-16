package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class SetupPinResponse {

    private String message;
}
