package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
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
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 400
                    && e.getResponse().hasBody()
                    && e.getResponse()
                            .getBody(String.class)
                            .contains(ErrorMessages.DNB_ERROR_WRONG_PSUID)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            } else {
                throw e;
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
                .header(DnbConstants.HeaderKeys.CONSENT_ID, consentId)
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

    public CardTransactionResponse fetchCardTransactions(String consentId, String cardAccountId) {
        return commonFetchTransactions(
                        consentId,
                        new URL(Urls.CARD_TRANSACTION).parameter(IdTags.ACCOUNT_ID, cardAccountId))
                .getBody(CardTransactionResponse.class);
    }

    public TransactionResponse fetchTransactions(String consentId, String accountId) {
        return commonFetchTransactions(
                        consentId,
                        new URL(Urls.TRANSACTIONS).parameter(IdTags.ACCOUNT_ID, accountId))
                .getBody(TransactionResponse.class);
    }

    private HttpResponse commonFetchTransactions(String consentId, URL url) {
        return createRequest(url)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(QueryKeys.FROM_DATE, "1970-01-01")
                .queryParam(
                        QueryKeys.TO_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(
                                localDateTimeSource.getInstant()))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .get(HttpResponse.class);
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
