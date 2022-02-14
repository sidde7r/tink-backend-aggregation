package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
@Slf4j
public class CbiGlobePaymentApiClient {

    private final CbiGlobeHttpClient client;
    private final CbiUrlProvider urlProvider;
    private final CbiGlobeProviderConfiguration providerConfiguration;

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, Payment payment) {
        URL url =
                urlProvider
                        .getPaymentsUrl()
                        .parameter(PathVariables.PAYMENT_SERVICE, getPaymentService(payment))
                        .parameter(PathVariables.PAYMENT_PRODUCT, getPaymentProduct(payment));
        log.info("CBI debug - url for createPayment is " + url.get());

        String okFullRedirectUrl = client.buildRedirectUri(true);
        String nokFullRedirectUrl = client.buildRedirectUri(false);

        RequestBuilder requestBuilder =
                client.createRequestInSessionWithPsuIp(url)
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode())
                        .header(HeaderKeys.TPP_REDIRECT_PREFERRED, true)
                        .header(HeaderKeys.TPP_REDIRECT_URI, okFullRedirectUrl)
                        .header(HeaderKeys.TPP_NOK_REDIRECT_URI, nokFullRedirectUrl);

        return client.makeRequest(
                requestBuilder,
                HttpMethod.POST,
                CreatePaymentResponse.class,
                RequestContext.PAYMENT_CREATE,
                createPaymentRequest);
    }

    public CreatePaymentResponse getPayment(Payment payment) {
        URL url =
                urlProvider
                        .getFetchPaymentUrl()
                        .parameter(PathVariables.PAYMENT_SERVICE, getPaymentService(payment))
                        .parameter(PathVariables.PAYMENT_PRODUCT, getPaymentProduct(payment))
                        .parameter(PathVariables.PAYMENT_ID, payment.getUniqueId());
        log.info("CBI debug - url for getPayment is " + url.get());

        RequestBuilder requestBuilder =
                client.createRequestInSessionWithPsuIp(url)
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode());

        return client.makeRequest(
                requestBuilder,
                HttpMethod.GET,
                CreatePaymentResponse.class,
                RequestContext.PAYMENT_GET,
                null);
    }

    public CreatePaymentResponse getPaymentStatus(Payment payment) {
        URL url =
                urlProvider
                        .getFetchPaymentStatusUrl()
                        .parameter(PathVariables.PAYMENT_SERVICE, getPaymentService(payment))
                        .parameter(PathVariables.PAYMENT_PRODUCT, getPaymentProduct(payment))
                        .parameter(PathVariables.PAYMENT_ID, payment.getUniqueId());
        log.info("CBI debug - url for getPaymentStatus is " + url.get());
        RequestBuilder requestBuilder = client.createRequestInSession(url);

        return client.makeRequest(
                requestBuilder,
                HttpMethod.GET,
                CreatePaymentResponse.class,
                RequestContext.PAYMENT_STATUS_GET,
                null);
    }

    private String getPaymentProduct(Payment payment) {
        return PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                ? "instant-sepa-credit-transfers"
                : "sepa-credit-transfers";
    }

    private String getPaymentService(Payment payment) {
        return PaymentServiceType.PERIODIC == payment.getPaymentServiceType()
                ? "periodic-payments"
                : "payments";
    }
}
