package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDevice3RequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice3Request {
    private RegisterDevice3RequestPayload payload;

    public RegisterDevice3Request(final RegisterDevice3RequestPayload payload) {
        this.payload = Objects.requireNonNull(payload);
    }
}
