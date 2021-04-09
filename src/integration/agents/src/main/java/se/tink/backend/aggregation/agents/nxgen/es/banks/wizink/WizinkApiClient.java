package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.CardDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.CardDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.FindMovementsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.FindMovementsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.OtpRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class WizinkApiClient {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final TinkHttpClient httpClient;
    private final WizinkStorage wizinkStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public WizinkApiClient(
            TinkHttpClient httpClient,
            WizinkStorage wizinkStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.httpClient = httpClient;
        this.wizinkStorage = wizinkStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public CustomerLoginResponse login(CustomerLoginRequest request) {
        try {
            HttpResponse response =
                    httpClient
                            .request(Urls.LOGIN)
                            .headers(prepareHeaders())
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .body(request)
                            .post(HttpResponse.class);
            storeSessionValues(response);
            return response.getBody(CustomerLoginResponse.class);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
            throw LoginError.DEFAULT_MESSAGE.exception(e);
        }
    }

    public CardDetailResponse fetchCreditCardDetails(CardEntity cardEntity) {
        return createRequest(Urls.CARD_DETAIL)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(
                        new CardDetailRequest(
                                cardEntity.getAccountNumber(), cardEntity.getCardNumber()))
                .post(CardDetailResponse.class);
    }

    public FindMovementsResponse fetchCreditCardTransactionsFrom90Days(String accountNumber) {
        return createRequest(Urls.CARD_DETAIL_TRANSACTIONS)
                .body(new FindMovementsRequest(accountNumber, false, prepareDate89DaysAgo()))
                .post(FindMovementsResponse.class);
    }

    public FindMovementsResponse prepareOtpRequestToUserMobilePhone(String accountNumber) {
        return createRequest(Urls.CARD_DETAIL_TRANSACTIONS)
                .body(new FindMovementsRequest(accountNumber, true, prepareDate89DaysAgo()))
                .post(FindMovementsResponse.class);
    }

    public FindMovementsResponse fetchCreditCardTransactionsOlderThan90Days(
            String accountNumber, String sessionId) {
        String otpInput = supplementalInformationHelper.waitForOtpInput();
        try {
            return createRequest(Urls.CARD_DETAIL_TRANSACTIONS)
                    .body(
                            new FindMovementsRequest(
                                    accountNumber,
                                    new OtpRequest(otpInput, sessionId),
                                    prepareDate89DaysAgo()))
                    .post(FindMovementsResponse.class);
        } catch (HttpResponseException e) {
            FindMovementsResponse response = e.getResponse().getBody(FindMovementsResponse.class);
            if (ErrorCodes.WRONG_OTP.equalsIgnoreCase(response.getResult().getCode())) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }
            log.warn(
                    "Unknown error code {} with message {}",
                    response.getResult().getCode(),
                    response.getResult().getMessage());
            throw e;
        }
    }

    public void logout() {
        createRequest(WizinkConstants.Urls.LOGOUT).post();
    }

    public boolean isAlive() {
        try {
            createRequest(Urls.KEEP_ALIVE).get(HttpResponse.class);
        } catch (HttpResponseException e) {
            return false;
        }
        return true;
    }

    private Map<String, Object> prepareHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.CONNECTION_TYPE, "WIFI");
        headers.put(HeaderKeys.MACHINE_TYPE, "iPhone");
        headers.put(HeaderKeys.OPERATIVE_SYSTEM, "12.4.5");
        headers.put(HeaderKeys.LOGIN_TYPE, "001");
        headers.put(HeaderKeys.DEVICE_ID, wizinkStorage.getDeviceId());
        headers.put(HeaderKeys.INDIGITALL_DEVICE, wizinkStorage.getIndigitallDevice());
        return headers;
    }

    private void storeSessionValues(HttpResponse response) {
        extractHeaderValue(response.getHeaders(), HeaderKeys.X_TOKEN_ID)
                .ifPresent(wizinkStorage::storeXTokenId);
        extractHeaderValue(response.getHeaders(), HeaderKeys.X_TOKEN_USER)
                .ifPresent(wizinkStorage::storeXTokenUser);
    }

    private Optional<String> extractHeaderValue(
            MultivaluedMap<String, String> headers, String headerName) {
        return Optional.ofNullable(headers).map(h -> h.getFirst(headerName));
    }

    private RequestBuilder createRequest(String url) {
        return httpClient
                .request(url)
                .header(HeaderKeys.X_TOKEN_ID, wizinkStorage.getXTokenId())
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    private String prepareDate89DaysAgo() {
        LocalDate date89DaysAgo = LocalDate.now().minusDays(89);
        return date89DaysAgo.format(DATE_FORMATTER);
    }
}
