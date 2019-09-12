package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchCustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util.Utils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SbabApiClient {

    private final TinkHttpClient client;
    private SbabConfiguration configuration;
    private final PersistentStorage persistentStorage;

    public SbabApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected void setConfiguration(SbabConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public FetchAccountResponse fetchAccounts() {
        return client.request(Urls.ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .get(FetchAccountResponse.class);
    }

    public FetchCustomerResponse fetchCustomer() {
        return client.request(Urls.CUSTOMERS)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .get(FetchCustomerResponse.class);
    }

    private OAuth2Token getToken() {
        return persistentStorage
                .get(PersistentStorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Expected token to be present"));
    }

    public FetchTransactionsResponse fetchTransactions(
            TransactionalAccount account, Date startDate, Date endDate) {
        FetchTransactionsResponse response =
                client.request(
                                Urls.TRANSACTIONS.parameter(
                                        IdTags.ACCOUNT_NUMBER,
                                        account.getFromTemporaryStorage(
                                                StorageKeys.ACCOUNT_NUMBER)))
                        .queryParam(
                                QueryKeys.END_DATE,
                                Utils.formatDateTime(endDate, Format.TIMESTAMP, Format.TIMEZONE))
                        .queryParam(
                                QueryKeys.START_DATE,
                                Utils.formatDateTime(startDate, Format.TIMESTAMP, Format.TIMEZONE))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                        .get(FetchTransactionsResponse.class);
        response.setPersistentStorage(persistentStorage);
        return response;
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, String debtorAccountNumber) {
        return createRequest(
                        Urls.INITIATE_PAYMENT.parameter(IdTags.ACCOUNT_NUMBER, debtorAccountNumber))
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String transferId, String debtorId) {
        return createRequest(
                        Urls.GET_PAYMENT
                                .parameter(IdTags.ACCOUNT_NUMBER, debtorId)
                                .parameter(IdTags.PAYMENT_ID, transferId))
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .get(GetPaymentResponse.class);
    }

    public BankIdResponse initBankId(String ssn) {
        return client.request(Urls.AUTHORIZATION)
                .header(HeaderKeys.PSU_IP_ADDRESS, "")
                .queryParam(QueryKeys.USER_ID, ssn)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(BankIdResponse.class);
    }

    public DecoupledResponse getDecoupled(String code) {
        TokenRequest tokenRequest =
                new TokenRequest(
                        configuration.getRedirectUrl(),
                        code,
                        QueryValues.PENDING_AUTHORIZATION_CODE);
        return client.request(Urls.TOKEN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(DecoupledResponse.class, tokenRequest.toData());
    }

    public DecoupledResponse refreshAccessToken(String refreshToken) {
        RefreshTokenRequest tokenRequest =
                new RefreshTokenRequest(QueryValues.REFRESH_TOKEN, refreshToken);
        return client.request(Urls.TOKEN)
                .header(HeaderKeys.PSU_IP_ADDRESS, "")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(DecoupledResponse.class, tokenRequest.toData());
    }
}
