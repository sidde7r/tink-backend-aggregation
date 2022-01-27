package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.util.List;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.GetPaymentRequest;
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
    private final PaymentRequestResourceFactory paymentRequestResourceFactory;

    public LclPaymentApiClient(
            SessionStorage sessionStorage,
            AgentConfiguration<LclConfiguration> configuration,
            LclRequestFactory lclRequestFactory,
            TokenFetcher tokenFetcher) {
        this(
                sessionStorage,
                configuration,
                lclRequestFactory,
                tokenFetcher,
                new PaymentRequestResourceFactory());
    }

    public LclPaymentApiClient(
            SessionStorage sessionStorage,
            AgentConfiguration<LclConfiguration> configuration,
            LclRequestFactory lclRequestFactory,
            TokenFetcher tokenFetcher,
            PaymentRequestResourceFactory paymentRequestResourceFactory) {
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
        this.lclRequestFactory = lclRequestFactory;
        this.tokenFetcher = tokenFetcher;
        this.paymentRequestResourceFactory = paymentRequestResourceFactory;
    }

    @Override
    public void fetchToken() {
        tokenFetcher.fetchToken();
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        PaymentRequestResource paymentRequestResource =
                paymentRequestResourceFactory.createPaymentRequestResource(request);

        sessionStorage.put(
                REDIRECT_URL_LOCAL_KEY,
                request.getSupplementaryData().getSuccessfulReportUrl() + CODE_CODE);
        HttpResponse httpResponse =
                lclRequestFactory
                        .createPaymentRequest(paymentRequestResource)
                        .post(
                                HttpResponse.class,
                                paymentRequestResourceFactory.serializeBody(
                                        paymentRequestResource));
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
        GetPaymentRequest getPaymentRequest =
                lclRequestFactory.getPaymentRequest(paymentId).get(GetPaymentRequest.class);
        String statusCode = getPaymentRequest.getPaymentRequest().getPaymentInformationStatus();
        return isStatusToBeConfirmed(statusCode)
                ? confirmPaymentRequest(paymentId).getPaymentRequest().toPaymentResponse()
                : getPaymentRequest.getPaymentRequest().toPaymentResponse();
    }

    private boolean isStatusToBeConfirmed(String statusCode) {
        return statusCode != null
                && statusCode.equalsIgnoreCase(LCL_PAYMENT_STATUS_WAITING_FOR_CONFIRMATION);
    }

    private GetPaymentRequest confirmPaymentRequest(String paymentId) {
        ConfirmPaymentRequest confirmPaymentRequest =
                lclRequestFactory.createConfirmPaymentRequest();
        return lclRequestFactory
                .confirmPaymentRequest(paymentId, confirmPaymentRequest)
                .post(GetPaymentRequest.class, confirmPaymentRequest);
    }
}
