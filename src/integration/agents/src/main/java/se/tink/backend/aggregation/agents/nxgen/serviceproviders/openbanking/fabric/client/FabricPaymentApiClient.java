package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.PathParameterValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.FabricPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.FabricPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.PaymentAuthorizationStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.PaymentAuthorizationsResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
public class FabricPaymentApiClient {

    private final FabricRequestBuilder requestBuilder;

    private final SessionStorage sessionStorage;
    private final String redirectUrl;

    public FabricPaymentResponse createPayment(
            FabricPaymentRequest fabricPaymentRequest, Payment payment) {
        return requestBuilder
                .createRequest(
                        new URL(Urls.INITIATE_A_PAYMENT_URL)
                                .parameter(
                                        PathParameterKeys.PAYMENT_SERVICE,
                                        getPaymentService(payment))
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        getPaymentProduct(payment)))
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, HeaderValues.TPP_REDIRECT_PREFERED)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(redirectUrl)
                                .queryParam(QueryKeys.CODE, QueryValues.CODE)
                                .queryParam(QueryKeys.STATE, sessionStorage.get(QueryKeys.STATE)))
                .post(FabricPaymentResponse.class, fabricPaymentRequest);
    }

    public FabricPaymentResponse getPayment(Payment payment) {
        return requestBuilder
                .createRequest(
                        new URL(Urls.PAYMENT_URL)
                                .parameter(
                                        PathParameterKeys.PAYMENT_SERVICE,
                                        getPaymentService(payment))
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        getPaymentProduct(payment))
                                .parameter(PathParameterKeys.PAYMENT_ID, payment.getUniqueId()))
                .type(MediaType.APPLICATION_JSON)
                .get(FabricPaymentResponse.class);
    }

    public FabricPaymentResponse getPaymentStatus(Payment payment) {
        return requestBuilder
                .createRequest(
                        new URL(Urls.GET_PAYMENT_STATUS_URL)
                                .parameter(
                                        PathParameterKeys.PAYMENT_SERVICE,
                                        getPaymentService(payment))
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        getPaymentProduct(payment))
                                .parameter(PathParameterKeys.PAYMENT_ID, payment.getUniqueId()))
                .get(FabricPaymentResponse.class);
    }

    public PaymentAuthorizationsResponse getPaymentAuthorizations(Payment payment) {
        PaymentAuthorizationsResponse result =
                requestBuilder
                        .createRequest(
                                new URL(Urls.GET_PAYMENT_AUTHORIZATIONS_URL)
                                        .parameter(
                                                PathParameterKeys.PAYMENT_SERVICE,
                                                getPaymentService(payment))
                                        .parameter(
                                                PathParameterKeys.PAYMENT_PRODUCT,
                                                getPaymentProduct(payment))
                                        .parameter(
                                                PathParameterKeys.PAYMENT_ID,
                                                payment.getUniqueId()))
                        .get(PaymentAuthorizationsResponse.class);
        if (!result.getAuthorisationIds().isEmpty()) {
            sessionStorage.put(
                    StorageKeys.PAYMENT_AUTHORIZATION_ID, result.getLastAuthorisationId());
        }
        return result;
    }

    public FabricPaymentResponse deletePayment(Payment payment) {
        return requestBuilder
                .createRequest(
                        new URL(Urls.PAYMENT_URL)
                                .parameter(
                                        PathParameterKeys.PAYMENT_SERVICE,
                                        getPaymentService(payment))
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        getPaymentProduct(payment))
                                .parameter(PathParameterKeys.PAYMENT_ID, payment.getUniqueId()))
                .type(MediaType.APPLICATION_JSON)
                .delete(FabricPaymentResponse.class);
    }

    public PaymentAuthorizationStatus getPaymentAuthorizationStatus(Payment payment) {
        return requestBuilder
                .createRequest(
                        new URL(Urls.GET_PAYMENT_AUTHORIZATION_STATUS_URL)
                                .parameter(
                                        PathParameterKeys.PAYMENT_SERVICE,
                                        getPaymentService(payment))
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        getPaymentProduct(payment))
                                .parameter(PathParameterKeys.PAYMENT_ID, payment.getUniqueId())
                                .parameter(
                                        PathParameterKeys.PAYMENT_AUTHORIZATION_ID,
                                        sessionStorage.get(StorageKeys.PAYMENT_AUTHORIZATION_ID)))
                .get(PaymentAuthorizationStatus.class);
    }

    private String getPaymentService(Payment payment) {
        if (PaymentServiceType.PERIODIC == payment.getPaymentServiceType()) {
            return PathParameterValues.PAYMENT_SERVICE_PERIODIC_PAYMENTS;
        } else {
            return PathParameterValues.PAYMENT_SERVICE_PAYMENTS;
        }
    }

    private String getPaymentProduct(Payment payment) {
        if (PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()) {
            return PathParameterValues.PAYMENT_PRODUCT_SEPA_INSTANT;
        } else {
            return PathParameterValues.PAYMENT_PRODUCT_SEPA_CREDIT;
        }
    }
}
