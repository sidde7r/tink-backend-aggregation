package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.CodeExchangeForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.OAuthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.TokenRefreshForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity.TransferStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc.ExecutePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc.ExecutePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.util.PaymentSignature;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.rpc.PayeesResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class StarlingApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public StarlingApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public OAuth2Token exchangeCode(CodeExchangeForm codeExchangeForm) {
        return client.request(StarlingConstants.Url.GET_OAUTH2_TOKEN)
                .body(codeExchangeForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(OAuthTokenResponse.class)
                .toOauth2Token();
    }

    public OAuth2Token refreshAccessToken(TokenRefreshForm refreshForm) {
        return client.request(StarlingConstants.Url.GET_OAUTH2_TOKEN)
                .body(refreshForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(OAuthTokenResponse.class)
                .toOauth2Token();
    }

    public AccountsResponse fetchAccounts() {
        return request(StarlingConstants.Url.GET_ACCOUNTS).get(AccountsResponse.class);
    }

    public AccountHolderResponse fetchAccountHolder() {
        return request(StarlingConstants.Url.GET_ACCOUNT_HOLDER).get(AccountHolderResponse.class);
    }

    public AccountIdentifiersResponse fetchAccountIdentifiers(final String accountUid) {
        return request(
                        StarlingConstants.Url.GET_ACCOUNT_IDENTIFIERS.parameter(
                                StarlingConstants.UrlParams.ACCOUNT_UID, accountUid))
                .get(AccountIdentifiersResponse.class);
    }

    public AccountBalanceResponse fetchAccountBalance(final String accountUid) {
        return request(
                        StarlingConstants.Url.GET_ACCOUNT_BALANCE.parameter(
                                StarlingConstants.UrlParams.ACCOUNT_UID, accountUid))
                .get(AccountBalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(Date from, Date to) {

        return request(StarlingConstants.Url.GET_ANY_TRANSACTIONS)
                .queryParam(StarlingConstants.RequestKey.FROM, toFormattedDate(from))
                .queryParam(StarlingConstants.RequestKey.TO, toFormattedDate(to))
                .get(TransactionsResponse.class);
    }

    public PayeesResponse fetchPayees() {
        return request(StarlingConstants.Url.GET_PAYEES).get(PayeesResponse.class);
    }

    public ExecutePaymentResponse executeTransfer(
            ExecutePaymentRequest paymentRequest,
            PaymentSignature.Builder signatureBuilder,
            String accountUid,
            String categoryUid,
            OAuth2Token paymentToken) {

        URL targetURL =
                StarlingConstants.Url.PUT_PAYMENT
                        .parameter(StarlingConstants.UrlParams.ACCOUNT_UID, accountUid)
                        .parameter(StarlingConstants.UrlParams.CATEGORY_UID, categoryUid);

        PaymentSignature signature =
                signatureBuilder
                        .withAccessToken(paymentToken.getAccessToken())
                        .withRequestTarget(HttpMethod.PUT, targetURL.toUri())
                        .withPayload(SerializationUtils.serializeToString(paymentRequest))
                        .build();

        return client.request(targetURL)
                .header(HttpHeaders.AUTHORIZATION, signature.getAuthHeader())
                .header(StarlingConstants.HeaderKey.DIGEST, signature.getDigest())
                .header(StarlingConstants.HeaderKey.DATE, signature.getTimeStamp())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .put(ExecutePaymentResponse.class, paymentRequest);
    }

    public TransferStatusEntity checkTransferStatus(
            String paymentOrderUid, OAuth2Token paymentToken) {

        try {

            client.request(
                            StarlingConstants.Url.GET_PAYMENT_STATUS.parameter(
                                    StarlingConstants.UrlParams.PAYMENT_ORDER_UID, paymentOrderUid))
                    .header(HttpHeaders.AUTHORIZATION, paymentToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(HttpResponse.class);

            return TransferStatusEntity.Ok();

        } catch (HttpResponseException e) {
            int status = e.getResponse().getStatus();

            if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }

            if (status == HttpStatus.SC_BAD_REQUEST) {

                // Starling will send very specific error messages in readable text for bad
                // requests.
                return TransferStatusEntity.Fail(status, e.getResponse().getBody(String.class));
            }

            throw e;
        }
    }

    private RequestBuilder request(URL url) {
        return client.request(url)
                .header(HttpHeaders.AUTHORIZATION, this.getOAuthToken().toAuthorizeHeader())
                .accept(MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getOAuthToken() {

        return persistentStorage
                .get(StarlingConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private static String toFormattedDate(final Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(date);
    }
}
