package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import java.util.Arrays;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@AllArgsConstructor
public class DnbApiClient {

    private final TinkHttpClient client;
    private final DnbHeaderValues headerValues;
    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    // Auth methods

    public ConsentResponse createConsent(String state) {
        try {
            return createRequest(new URL(Urls.CONSENTS))
                    .header(
                            HeaderKeys.TPP_REDIRECT_URI,
                            new URL(headerValues.getRedirectUrl())
                                    .queryParam(QueryKeys.STATE, state))
                    .post(ConsentResponse.class, new ConsentRequest());
        } catch (HttpResponseException httpException) {
            handleKnownCreateConsentErrors(httpException);
            throw httpException;
        }
    }

    private void handleKnownCreateConsentErrors(HttpResponseException httpException) {
        if (Arrays.asList(400, 403).contains(httpException.getResponse().getStatus())
                && httpException.getResponse().hasBody()) {
            String body = httpException.getResponse().getBody(String.class);
            if (body.contains(ErrorMessages.WRONG_PSUID)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(httpException);
            }
            if (body.contains(ErrorMessages.NO_ACCOUNTS)) {
                throw LoginError.NO_ACCOUNTS.exception(httpException);
            }
        }
    }

    public ConsentDetailsResponse fetchConsentDetails(String consentId) {
        return createRequest(new URL(Urls.CONSENT_DETAILS).parameter(IdTags.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    // Fetcher related methods

    public AccountsResponse fetchAccounts(String consentId) {
        return createRequest(new URL(Urls.ACCOUNTS))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchBalances(String consentId, String accountId) {
        return createRequest(new URL(Urls.BALANCES).parameter(IdTags.ACCOUNT_ID, accountId))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .get(BalancesResponse.class);
    }

    public CardAccountResponse fetchCardAccounts(String consentId) {
        try {
            return createRequest(new URL(Urls.CARDS))
                    .header(HeaderKeys.CONSENT_ID, consentId)
                    .get(CardAccountResponse.class);
        } catch (HttpResponseException ex) {
            // DNB returns 404 if user has no cards, for some users
            if (ex.getResponse().getStatus() == 404) {
                return new CardAccountResponse();
            } else {
                throw ex;
            }
        }
    }

    public CardTransactionResponse fetchCardTransactions(
            String consentId, String cardAccountId, Date fromDate, Date toDate) {
        return createRequest(
                        new URL(Urls.CARD_TRANSACTION).parameter(IdTags.ACCOUNT_ID, cardAccountId))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        QueryKeys.FROM_DATE, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.TO_DATE, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .get(CardTransactionResponse.class);
    }

    public TransactionResponse fetchTransactions(
            String fromDate, String consentId, String accountId) {
        return createRequest(new URL(Urls.TRANSACTIONS).parameter(IdTags.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(QueryKeys.FROM_DATE, fromDate)
                .queryParam(
                        QueryKeys.TO_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(
                                localDateTimeSource.getInstant()))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .get(TransactionResponse.class);
    }

    public TransactionResponse fetchNextTransactions(String consentId, String nextUrl) {
        return createRequest(new URL(Urls.BASE_URL + nextUrl))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .get(TransactionResponse.class);
    }

    // Payment related methods

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, DnbPaymentType dnbPaymentType) {
        return createRequest(
                        new URL(Urls.PAYMENTS)
                                .parameter(IdTags.PAYMENT_TYPE, dnbPaymentType.toString()))
                .header(HeaderKeys.TPP_REDIRECT_URI, headerValues.getRedirectUrl())
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(DnbPaymentType dnbPaymentType, String paymentId) {
        return createRequest(
                        new URL(Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_TYPE, dnbPaymentType.toString())
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    // Common methods

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(HeaderKeys.PSU_ID, headerValues.getPsuId())
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp());
    }
}
