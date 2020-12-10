package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity.TransferStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc.ExecutePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc.ExecutePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.util.PaymentSignature;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.BusinessAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.IndividualAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.JointAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.SoleTraderAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.rpc.PayeesResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.Token;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class StarlingApiClient {

    private final TinkHttpClient client;
    private final AgentRedirectTokensAuthenticationPersistedData redirectTokensPersistedData;

    public StarlingApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        redirectTokensPersistedData =
                new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                                new ObjectMapperFactory().getInstance())
                        .createAgentRedirectTokensAuthenticationPersistedData(
                                new AgentAuthenticationPersistedData(persistentStorage));
    }

    public AccountsResponse fetchAccounts() {
        return request(StarlingConstants.Url.GET_ACCOUNTS).get(AccountsResponse.class);
    }

    public AccountHolderResponse fetchAccountHolder() {
        return request(Url.GET_ACCOUNT_HOLDER).get(AccountHolderResponse.class);
    }

    public IndividualAccountHolderResponse fetchIndividualAccountHolder() {
        return request(Url.GET_INDIVIDUAL_ACCOUNT_HOLDER)
                .get(IndividualAccountHolderResponse.class);
    }

    public JointAccountHolderResponse fetchJointAccountHolder() {
        return request(Url.GET_JOINT_ACCOUNT_HOLDER).get(JointAccountHolderResponse.class);
    }

    public SoleTraderAccountHolderResponse fetchSoleTraderAccountHolder() {
        return request(Url.GET_SOLE_TRADER_ACCOUNT_HOLDER)
                .get(SoleTraderAccountHolderResponse.class);
    }

    public BusinessAccountHolderResponse fetchBusinessAccountHolder() {
        return request(Url.GET_BUSINESS_ACCOUNT_HOLDER).get(BusinessAccountHolderResponse.class);
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

    public TransactionsResponse fetchTransactions(
            Date from, Date to, TransactionalAccount account) {

        URL targetURL =
                Url.GET_ANY_TRANSACTIONS
                        .parameter(UrlParams.ACCOUNT_UID, account.getApiIdentifier())
                        .parameter(
                                UrlParams.CATEGORY_UID,
                                account.getFromTemporaryStorage(UrlParams.CATEGORY_UID));

        return request(targetURL)
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
                    .addBearerToken(paymentToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(HttpResponse.class);

            return TransferStatusEntity.Ok();

        } catch (HttpResponseException e) {
            int status = e.getResponse().getStatus();

            if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
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
                .addBearerToken(this.getOAuthToken())
                .accept(MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getOAuthToken() {
        Token accessToken = redirectTokensPersistedData.getRedirectTokens().get().getAccessToken();
        return OAuth2Token.create(
                accessToken.getTokenType(),
                new String(accessToken.getBody(), StandardCharsets.UTF_8),
                null,
                accessToken.getExpiresInSeconds());
    }

    private static String toFormattedDate(final Date date) {
        return ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITHOUT_TIMEZONE.format(date);
    }
}
