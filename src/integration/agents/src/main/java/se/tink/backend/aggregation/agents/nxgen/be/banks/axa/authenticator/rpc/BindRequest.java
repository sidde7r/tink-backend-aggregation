package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AnonymousInvokeBindRequestData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BindRequest extends BaseRequest<AnonymousInvokeBindRequestData> {

    public BindRequest(AnonymousInvokeBindRequestData data, String uid) {
        super(data, Collections.singletonList(new HeaderEntity(uid)));
    }
}
