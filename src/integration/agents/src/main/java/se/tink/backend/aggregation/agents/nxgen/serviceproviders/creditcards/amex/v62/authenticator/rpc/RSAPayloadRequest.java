package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.ConstantValueHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.HeadersValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RSAPayloadRequest {
    @JsonProperty private String appId = HeadersValue.CLIENT_ID;
    @JsonProperty private String version = ConstantValueHeaders.OS_VERSION.getValue();
    @JsonProperty private String random;

    public RSAPayloadRequest(String random) {
        this.random = random;
    }
}
