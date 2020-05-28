package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.ConstantValueHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.HeadersValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceDataEntity {
    @JsonProperty("device_model")
    private String deviceModel = ConstantValueHeaders.CLIENT_TYPE.getValue();

    @JsonProperty("client_version")
    private String clientVersion = HeadersValue.CLIENT_VERSION;

    @JsonProperty("os_version")
    private String osVersion = ConstantValueHeaders.OS_VERSION.getValue();

    @JsonProperty("device_type")
    private String deviceType = HeadersValue.DEVICE_TYPE;
}
