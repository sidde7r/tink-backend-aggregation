package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.helper.PaymentUrlUtil;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class CbiGlobePaymentApiClient {

    private final CbiGlobeRequestBuilder cbiRequestBuilder;
    private final CbiUrlProvider urlProvider;
    private final CbiGlobeProviderConfiguration providerConfiguration;

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, Payment payment) {
        String okFullRedirectUrl = cbiRequestBuilder.buildRedirectUri(true);
        String nokFullRedirectUrl = cbiRequestBuilder.buildRedirectUri(false);

        RequestBuilder requestBuilder =
                cbiRequestBuilder
                        .createRequestInSession(
                                PaymentUrlUtil.fillCommonPaymentParams(
                                        urlProvider.getPaymentsUrl(), payment))
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode())
                        .header(HeaderKeys.TPP_REDIRECT_PREFERRED, true)
                        .header(HeaderKeys.TPP_REDIRECT_URI, okFullRedirectUrl)
                        .header(HeaderKeys.TPP_NOK_REDIRECT_URI, nokFullRedirectUrl);

        return cbiRequestBuilder.makeRequest(
                cbiRequestBuilder.addPsuIpAddressHeader(requestBuilder),
                HttpMethod.POST,
                CreatePaymentResponse.class,
                RequestContext.PAYMENT_CREATE,
                createPaymentRequest);
    }

    public CreatePaymentResponse getPayment(Payment payment) {
        RequestBuilder requestBuilder =
                cbiRequestBuilder
                        .createRequestInSession(
                                PaymentUrlUtil.fillCommonPaymentParams(
                                        urlProvider.getFetchPaymentUrl(), payment))
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode());

        return cbiRequestBuilder.makeRequest(
                cbiRequestBuilder.addPsuIpAddressHeader(requestBuilder),
                HttpMethod.GET,
                CreatePaymentResponse.class,
                RequestContext.PAYMENT_GET,
                null);
    }

    public CreatePaymentResponse getPaymentStatus(Payment payment) {
        RequestBuilder requestBuilder =
                cbiRequestBuilder.createRequestInSession(
                        PaymentUrlUtil.fillCommonPaymentParams(
                                urlProvider.getFetchPaymentStatusUrl(), payment));

        return cbiRequestBuilder.makeRequest(
                requestBuilder,
                HttpMethod.GET,
                CreatePaymentResponse.class,
                RequestContext.PAYMENT_STATUS_GET,
                null);
    }
}
