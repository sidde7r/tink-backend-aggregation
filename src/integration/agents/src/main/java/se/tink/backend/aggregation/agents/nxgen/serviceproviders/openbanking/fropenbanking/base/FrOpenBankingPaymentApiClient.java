package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;

public interface FrOpenBankingPaymentApiClient {

    void fetchToken();

    CreatePaymentResponse createPayment(CreatePaymentRequest request)
            throws PaymentValidationException;

    String findPaymentId(String authorizationUrl);

    GetPaymentResponse getPayment(String paymentId);
}
