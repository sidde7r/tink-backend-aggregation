package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentSigningResponse {
    private ArrayList<String> paymentIds;
}
