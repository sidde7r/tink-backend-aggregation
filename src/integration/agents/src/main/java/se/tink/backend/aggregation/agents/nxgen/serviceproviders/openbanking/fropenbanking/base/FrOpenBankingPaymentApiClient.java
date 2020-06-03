package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;

public interface FrOpenBankingPaymentApiClient {

    void fetchToken();

    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    GetPaymentResponse getPayment(String paymentId);
}
