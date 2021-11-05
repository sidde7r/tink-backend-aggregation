package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Constants.API_USER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.TRANSACTION_ID;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPayment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsSession;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class FinTecSystemsApiClient {

    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final String apiKey;
    private final String blz;
    private final String market;

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .addBasicAuth(API_USER_NAME, apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString());
    }

    public FinTechSystemsPaymentResponse createPayment(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        FinTechSystemsPaymentRequest finTechSystemsPaymentRequest =
                getCreatePaymentRequest(payment);

        return createRequest(Urls.PAYMENT_INITIATION)
                .post(FinTechSystemsPaymentResponse.class, finTechSystemsPaymentRequest);
    }

    private FinTechSystemsPaymentRequest getCreatePaymentRequest(Payment payment) {
        FinTechSystemsPaymentRequest finTechSystemsPaymentRequest =
                new FinTechSystemsPaymentRequest();
        finTechSystemsPaymentRequest.setAmount(
                payment.getExactCurrencyAmount().getExactValue().toString());
        finTechSystemsPaymentRequest.setCurrencyId(
                payment.getExactCurrencyAmount().getCurrencyCode());
        finTechSystemsPaymentRequest.setRecipientHolder(payment.getCreditor().getName());
        if (payment.getCreditor().getAccountIdentifier() instanceof IbanIdentifier) {
            finTechSystemsPaymentRequest.setRecipientIban(
                    ((IbanIdentifier) payment.getCreditor().getAccountIdentifier()).getIban());
        }
        finTechSystemsPaymentRequest.setPurpose(payment.getRemittanceInformation().getValue());
        finTechSystemsPaymentRequest.setSenderCountryId(market);
        finTechSystemsPaymentRequest.setSenderBankCode(blz);
        return finTechSystemsPaymentRequest;
    }

    public FinTechSystemsPayment fetchPaymentStatus(PaymentRequest paymentRequest) {
        return createRequest(
                        Urls.FETCH_PAYMENT_STATUS.parameter(
                                TRANSACTION_ID, paymentRequest.getPayment().getUniqueId()))
                .get(FinTechSystemsPayment.class);
    }

    public FinTechSystemsSession getSessionStatus(String transationId) {
        return createRequest(Urls.GET_SESSION_STATUS.parameter(TRANSACTION_ID, transationId))
                .get(FinTechSystemsSession.class);
    }
}
