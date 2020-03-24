package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AnonymousInvokeBindRequestData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AnonymousInvokeRequest extends BaseRequest<AnonymousInvokeBindRequestData> {

    public AnonymousInvokeRequest(AnonymousInvokeBindRequestData data) {
        super(data, Collections.emptyList());
    }
}
