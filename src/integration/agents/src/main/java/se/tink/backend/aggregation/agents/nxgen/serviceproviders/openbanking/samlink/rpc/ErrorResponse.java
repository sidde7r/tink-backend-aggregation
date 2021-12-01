package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private List<TppMessageEntity> tppMessages;
}
