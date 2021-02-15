package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.errors.rpc;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class KbcErrorMessage {
    private List<TppMessageEntity> tppMessages;
}
