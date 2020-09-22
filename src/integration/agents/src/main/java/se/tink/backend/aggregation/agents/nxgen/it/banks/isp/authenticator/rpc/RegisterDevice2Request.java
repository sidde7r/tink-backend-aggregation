package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDevice2RequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice2Request {
    private RegisterDevice2RequestPayload payload;

    public RegisterDevice2Request(final RegisterDevice2RequestPayload payload) {
        this.payload = Objects.requireNonNull(payload);
    }
}
