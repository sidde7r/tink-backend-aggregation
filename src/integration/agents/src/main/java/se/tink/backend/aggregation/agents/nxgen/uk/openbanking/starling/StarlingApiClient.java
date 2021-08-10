package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url.GET_PAYMENT_STATUS;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url.PUT_PAYMENT;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc.InstructLocalPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc.InstructLocalPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc.PaymentOrderPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.payment.StarlingPaymentAccountCategoryStorage;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderNameResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.BusinessAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.SoleTraderAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.StarlingAccountHolderType;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class StarlingApiClient {

    private final TinkHttpClient client;
    private final AgentRefreshableAccessTokenAuthenticationPersistedData
            redirectTokensPersistedData;
    private final StarlingPaymentAccountCategoryStorage starlingPaymentAccountCategoryStorage;

    public StarlingApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        redirectTokensPersistedData =
                new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                                new ObjectMapperFactory().getInstance())
                        .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                new AgentAuthenticationPersistedData(persistentStorage));
        starlingPaymentAccountCategoryStorage =
                new StarlingPaymentAccountCategoryStorage(persistentStorage);
    }

    public List<AccountEntity> fetchAccounts() {
        List<AccountEntity> accounts =
                request(Url.GET_ACCOUNTS).get(AccountsResponse.class).getAccounts();
        starlingPaymentAccountCategoryStorage.saveAccountUuidWithCategoryUuid(accounts);
        return accounts;
    }

    public StarlingAccountHolderType fetchAccountHolderType() {
        return request(Url.GET_ACCOUNT_HOLDER)
                .get(AccountHolderResponse.class)
                .getAccountHolderType();
    }

    public AccountHolderNameResponse fetchAccountHolderName() {
        return request(Url.GET_ACCOUNT_HOLDER_NAME).get(AccountHolderNameResponse.class);
    }

    public SoleTraderAccountHolderResponse fetchSoleTraderAccountHolder() {
        return request(Url.GET_SOLE_TRADER_ACCOUNT_HOLDER)
                .get(SoleTraderAccountHolderResponse.class);
    }

    public BusinessAccountHolderResponse fetchBusinessAccountHolder() {
        return request(Url.GET_BUSINESS_ACCOUNT_HOLDER).get(BusinessAccountHolderResponse.class);
    }

    public AccountIdentifiersResponse fetchAccountIdentifiers(final String accountUid) {
        AccountIdentifiersResponse accountIdentifiersResponse =
                request(Url.GET_ACCOUNT_IDENTIFIERS.parameter(UrlParams.ACCOUNT_UID, accountUid))
                        .get(AccountIdentifiersResponse.class);
        starlingPaymentAccountCategoryStorage.saveAccountIdentifierWithAccountUuid(
                accountIdentifiersResponse, accountUid);
        return accountIdentifiersResponse;
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

    private RequestBuilder request(URL url) {
        return client.request(url)
                .addBearerToken(this.getOAuthToken())
                .accept(MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getOAuthToken() {
        return redirectTokensPersistedData
                .getRefreshableAccessToken()
                .map(RefreshableAccessToken::getAccessToken)
                .map(
                        accessToken ->
                                OAuth2Token.create(
                                        accessToken.getTokenType(),
                                        new String(accessToken.getBody(), StandardCharsets.UTF_8),
                                        null,
                                        accessToken.getExpiresInSeconds()))
                .orElseThrow(IllegalStateException::new);
    }

    private static String toFormattedDate(final Date date) {
        return ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITHOUT_TIMEZONE.format(date);
    }

    public void saveOAuthToken(OAuth2Token oAuth2Token) {
        Token.TokenBuilder builder = new Token.TokenBuilder();
        Token token =
                builder.body(oAuth2Token.getAccessToken())
                        .tokenType(oAuth2Token.getTokenType())
                        .expiresIn(oAuth2Token.getIssuedAt(), oAuth2Token.getExpiresInSeconds())
                        .build();
        redirectTokensPersistedData.storeRefreshableAccessToken(
                RefreshableAccessToken.builder().accessToken(token).build());
    }

    public URL getAccountCategoryUri(String accountNumber) {
        String accountId;
        String categoryId;
        if (!starlingPaymentAccountCategoryStorage.hasAccountsCached()) {
            this.fetchAccounts()
                    .forEach(
                            accountEntity ->
                                    this.fetchAccountIdentifiers(accountEntity.getAccountUid()));
        }
        accountId = starlingPaymentAccountCategoryStorage.getAccountUuid(accountNumber);
        categoryId = starlingPaymentAccountCategoryStorage.getCategoryUuid(accountId);
        return PUT_PAYMENT
                .parameter(UrlParams.ACCOUNT_UID, accountId)
                .parameter(UrlParams.CATEGORY_UID, categoryId);
    }

    public PaymentResponse createPayment(
            InstructLocalPaymentRequest instructLocalPaymentRequest,
            URL paymentUrl,
            Map<String, Object> headers)
            throws PaymentException {
        try {
            return client.request(paymentUrl)
                    .headers(headers)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(InstructLocalPaymentResponse.class, instructLocalPaymentRequest)
                    .toPaymentResponse();
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response != null && response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
                if (errorResponse != null
                        && errorResponse.errorContains(StarlingConstants.ErrorCode.INVALID_CREDITOR)
                        && !errorResponse.isSuccess()) {
                    throw new CreditorValidationException(
                            CreditorValidationException.DEFAULT_MESSAGE,
                            InternalStatus.INVALID_DESTINATION_ACCOUNT);
                }
            }
            throw e;
        }
    }

    public PaymentResponse fetchPayment(String paymentId) {
        return client.request(GET_PAYMENT_STATUS.parameter(UrlParams.PAYMENT_ORDER_UID, paymentId))
                .header(StarlingConstants.HeaderKey.AUTH, this.getOAuthToken().toAuthorizeHeader())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(PaymentOrderPaymentsResponse.class)
                .toPaymentResponse();
    }
}
