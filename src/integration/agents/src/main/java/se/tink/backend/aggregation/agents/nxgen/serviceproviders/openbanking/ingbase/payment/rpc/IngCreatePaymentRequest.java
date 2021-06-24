package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class IngCreatePaymentRequest extends CreatePaymentRequest {

    private final String endToEndIdentification;
    private final String creditorAgent;
    private final String chargeBearer;
    private final String serviceLevelCode;
    private final String localInstrumentCode;
}
