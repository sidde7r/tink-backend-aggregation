package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ResponseDataEntity {
    private String challenge;

    @JsonProperty("control_flow")
    private List<ControlFlowEntity> controlFlow;
}
