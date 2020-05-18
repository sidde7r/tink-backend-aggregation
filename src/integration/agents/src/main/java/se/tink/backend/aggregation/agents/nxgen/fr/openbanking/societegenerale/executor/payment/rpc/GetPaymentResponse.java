package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.GetPaymentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentRequest;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class GetPaymentResponse {

    private CreatePaymentRequest paymentRequest;

    @JsonProperty("_links")
    private GetPaymentLinksEntity links;
}
