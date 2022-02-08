package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmablePayment;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util.UrlParseUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
@Slf4j
public class LclPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private static final String PAYMENT_AUTHORIZATION_URL = "payment_authorization_url";
    private static final String REDIRECT_URL_LOCAL_KEY = "FULL_REDIRECT_URL";
    private static final String PAYMENT_ID_LOCAL_KEY = "PAYMENT_ID";
    private static final String LCL_PAYMENT_STATUS_WAITING_FOR_CONFIRMATION = "ACTC";
    private static final String LOCATION = "location";
    private static final String CODE_CODE = "&code=code";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";

    private final SessionStorage sessionStorage;
    private final AgentConfiguration<LclConfiguration> configuration;
    private final LclRequestFactory lclRequestFactory;
    private final TokenFetcher tokenFetcher;

    @Override
    public void fetchToken() {
        tokenFetcher.fetchToken();
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        sessionStorage.put(
                REDIRECT_URL_LOCAL_KEY,
                request.getSupplementaryData().getSuccessfulReportUrl() + CODE_CODE);
        HttpResponse httpResponse =
                lclRequestFactory
                        .createPaymentRequest(request)
                        .post(HttpResponse.class);
        List<String> locationHeader = httpResponse.getHeaders().get(LOCATION);
        if (locationHeader.isEmpty()) {
            throw new MissingLocationException("Location does not exist in the headers");
        }

        sessionStorage.put(PAYMENT_ID_LOCAL_KEY, UrlParseUtil.idFromUrl(locationHeader.get(0)));
        return httpResponse.getBody(CreatePaymentResponse.class);
    }

    @SneakyThrows
    @Override
    public String findPaymentId(String authorizationUrl) {
        URL url = new URL(authorizationUrl);
        sessionStorage.put(
                PAYMENT_AUTHORIZATION_URL,
                url.queryParam(
                                CLIENT_ID,
                                configuration.getProviderSpecificConfiguration().getClientId())
                        .queryParam(REDIRECT_URI, sessionStorage.get(REDIRECT_URL_LOCAL_KEY))
                        .toString());
        return sessionStorage.get(PAYMENT_ID_LOCAL_KEY);
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        ConfirmablePayment confirmablePayment =
                lclRequestFactory.getPaymentRequest(paymentId).get(ConfirmablePayment.class);
        ConfirmablePayment confirmedPayment = confirmPaymentIfNeeded(paymentId, confirmablePayment);
        return confirmedPayment.getPaymentRequest().toPaymentResponse();
    }

    private ConfirmablePayment confirmPaymentIfNeeded(
            String paymentId, ConfirmablePayment confirmablePayment) {
        String paymentInformationStatus = confirmablePayment.getPaymentRequest().getPaymentInformationStatus();
        return isStatusToBeConfirmed(paymentInformationStatus) ? confirmPaymentRequest(paymentId) : confirmablePayment;
    }

    private boolean isStatusToBeConfirmed(String statusCode) {
        return statusCode != null
                && statusCode.equalsIgnoreCase(LCL_PAYMENT_STATUS_WAITING_FOR_CONFIRMATION);
    }

    private ConfirmablePayment confirmPaymentRequest(String paymentId) {
        return lclRequestFactory.confirmPaymentRequest(paymentId).post(ConfirmablePayment.class);
    }
}
