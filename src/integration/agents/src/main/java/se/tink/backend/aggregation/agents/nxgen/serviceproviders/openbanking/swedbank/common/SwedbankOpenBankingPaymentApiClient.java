package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;

public interface SwedbankOpenBankingPaymentApiClient {
    CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, SwedbankPaymentType swedbankPaymentType);

    PaymentAuthorisationResponse startPaymentAuthorisation(
            String paymentId,
            SwedbankPaymentType swedbankPaymentType,
            String state,
            boolean isRedirect);

    PaymentStatusResponse getPaymentStatus(
            String paymentId, SwedbankPaymentType swedbankPaymentType);

    GetPaymentResponse getPayment(String paymentId, SwedbankPaymentType swedbankPaymentType);

    AuthenticationResponse startPaymentAuthorization(String endpoint);
}
