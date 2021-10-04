package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Constants.API_USER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.TRANSACTION_ID;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.GetSessionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class FinTecSystemsApiClient {

    private final FinTecSystemsConfiguration providerConfiguration;
    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final Provider provider;

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .addBasicAuth(API_USER_NAME, providerConfiguration.getApiKey())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString());
    }

    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        CreatePaymentRequest createPaymentRequest = getCreatePaymentRequest(payment);

        return createRequest(Urls.PAYMENT_INITIATION)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    private CreatePaymentRequest getCreatePaymentRequest(Payment payment) {
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest();
        createPaymentRequest.setAmount(payment.getExactCurrencyAmount().getExactValue().toString());
        createPaymentRequest.setCurrencyId(payment.getExactCurrencyAmount().getCurrencyCode());
        createPaymentRequest.setRecipientHolder(payment.getCreditor().getName());
        if (payment.getCreditor().getAccountIdentifier() instanceof IbanIdentifier) {
            createPaymentRequest.setRecipientIban(
                    ((IbanIdentifier) payment.getCreditor().getAccountIdentifier()).getIban());
        }
        createPaymentRequest.setPurpose(payment.getRemittanceInformation().getValue());
        createPaymentRequest.setSenderBic(provider.getPayload());
        return createPaymentRequest;
    }

    public GetPaymentResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        return createRequest(
                        Urls.FETCH_PAYMENT_STATUS.parameter(
                                TRANSACTION_ID, paymentRequest.getPayment().getUniqueId()))
                .get(GetPaymentResponse.class);
    }

    public GetSessionsResponse getSessionStatus(String transationId) {
        return createRequest(Urls.GET_SESSION_STATUS.parameter(TRANSACTION_ID, transationId))
                .get(GetSessionsResponse.class);
    }
}
