package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentResponse extends BasePaymentResponse {
    private String reasonCode;
    private String reason;
}
