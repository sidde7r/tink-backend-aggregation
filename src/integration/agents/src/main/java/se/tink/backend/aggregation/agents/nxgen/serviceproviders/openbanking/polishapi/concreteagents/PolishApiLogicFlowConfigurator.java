package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature.PolishApiSignatureFilter;

@Builder
public class PolishApiLogicFlowConfigurator {

    // Flags are set to values compatible with Polish API Standard
    // If your bank has some other logic just override needed flags.
    // Some of the flags are used only in Post API client.
    @Builder.Default private boolean shouldGetAccountListFromTokenResponse = false;
    @Builder.Default private boolean shouldAttachHeadersAndUriInJws = false;
    @Builder.Default private boolean shouldSentClientIdInRequestHeaderBody = false;
    @Builder.Default private boolean doesSupportExchangeToken = true;
    @Builder.Default private boolean shouldAddBearerStringInTokenInRequestBody = false;
    @Builder.Default private boolean shouldSentSingleScopeLimitInAisAccounts = false;
    @Builder.Default private boolean canCombineAisAndAisAccountsScopes = false;
    @Builder.Default private boolean doesSupportTransactionDateFrom = true;
    @Builder.Default private boolean shouldSentAuthorizationCodeInUpperCaseField = true;
    @Builder.Default private boolean shouldSentScopeAndScopeDetailsInFirstTokenRequest = true;
    @Builder.Default private boolean shouldSentScopeInRefreshTokenRequest = true;
    @Builder.Default private boolean shouldGenerateNewConsentIdInExchangeToken = false;
    @Builder.Default private boolean shouldSentTokenInRefreshAndExchangeToken = true;
    @Builder.Default private boolean shouldSentCompanyContextInTransactions = true;
    @Builder.Default private boolean shouldSentPageIdInFirstRequestAs0 = false;
    @Builder.Default private boolean shouldSendDatesInPendingTransactions = false;

    /**
     * All of banks in Token response return a list of accounts number. But some of them does not
     * support getting accounts from the accounts endpoint or does not provide necessary data to
     * fetch account details - therefore in account details fetch the numbers are fetched from
     * persistent storage instead of accounts endpoint.
     *
     * @see PolishApiAuthenticator#refreshAccessToken(java.lang.String)
     * @see PolishApiTransactionalAccountFetcher#fetchAccountDetailsFromListOfAccountNumbers()
     * @return information if bank returns list of accounts in token response
     */
    public boolean shouldGetAccountListFromTokenResponse() {
        return shouldGetAccountListFromTokenResponse;
    }

    /**
     * All of banks require signing requests and sending information either in X-JWS-SIGNATURE or
     * JWS-SIGNATURE header. There are banks which needs to sent only signed payload and there are
     * some that needs list of headers and URI.
     *
     * @see PolishApiConstants.JwsHeaders#JWS_HEADERS
     * @see PolishApiSignatureFilter
     * @return information if we should attach headers and uri in signature
     */
    public boolean shouldAttachHeadersAndUriInJws() {
        return shouldAttachHeadersAndUriInJws;
    }

    /**
     * Currently only BNP Paribas needs to have clientId sent in requestHeader body.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return information if we should attach client id in the requestHeader entity in the
     *     requests.
     */
    public boolean shouldSentClientIdInRequestHeaderBody() {
        return shouldSentClientIdInRequestHeaderBody;
    }

    /**
     * Some of banks support exchange token flow - which allows us to change token with AIS_ACCOUNTS
     * consent to AIS. This operation is not reversible - so for banks which supports the flow we
     * are fetching accounts after auth part and saves them to persistent storage.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @see PolishApiAuthenticator#exchangeAuthorizationCode(java.lang.String)
     * @return Information if bank supports exchange token flow
     */
    public boolean doesSupportExchangeToken() {
        return doesSupportExchangeToken;
    }

    /**
     * Some of the banks require providing "Bearer" phrase before token in the body. When this flag
     * is set to true then Bearer is added.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if bank needs Bearer in the requestHeader.token request.
     */
    public boolean shouldAddBearerStringInTokenInRequestBody() {
        return shouldAddBearerStringInTokenInRequestBody;
    }

    /**
     * Some of the banks require providing single scope in the authorize request in ais accounts and
     * some are fine with multiple.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if should sent single in scope limits
     */
    public boolean shouldSentSingleScopeLimitInAisAccounts() {
        return shouldSentSingleScopeLimitInAisAccounts;
    }

    /**
     * Some banks allow mixing ais and ais-accounts in privileges lists. For that case ais scope is
     * sent.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if bank allows to mix ais and ais-accounts scopes
     */
    public boolean canCombineAisAndAisAccountsScopes() {
        return canCombineAisAndAisAccountsScopes;
    }

    /**
     * Some banks allow to fetch transactions using params: transactionDateFrom, transactionDateTo
     * and some uses bookingDateFrom, bookingDateTo
     *
     * <p>USED ONLY IN POST API!!!
     *
     * <p>If your bank does not support transactionDateFrom(To) return false.
     *
     * @return Information if bank supports transactionDateFrom(to).
     */
    public boolean doesSupportTransactionDateFrom() {
        return doesSupportTransactionDateFrom;
    }

    /**
     * In general PolishAPI standard - handles authorization_code in TokenRequest in Code field.
     * However some of the banks changed Code to code.
     *
     * <p>If code is needed in lowercase set this value to false.
     *
     * @return Information information if code needs to be sent in UpperCase manner.
     */
    public boolean shouldSentAuthorizationCodeInUpperCaseField() {
        return shouldSentAuthorizationCodeInUpperCaseField;
    }

    /**
     * Scope and scope details are first defined in AuthorizeRequest and some of the banks require
     * to copy them in TokenRequest and some of them prohibits that - in general documentations of
     * banks might be misleading - because they state that the fields are required.
     *
     * <p>If your bank returns information that scope or scope details are invalid set this value to
     * false.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if agent should sent scope and scope details in first token request.
     */
    public boolean shouldSentScopeAndScopeDetailsInFirstTokenRequest() {
        return shouldSentScopeAndScopeDetailsInFirstTokenRequest;
    }

    /**
     * Most of the banks want to sent scope in refresh token (without details).
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if agent should sent scope in refresh token
     */
    public boolean shouldSentScopeInRefreshTokenRequest() {
        return shouldSentScopeInRefreshTokenRequest;
    }

    /**
     * Due to the fact that polish api standard can be interpreted in many different ways some of
     * the banks require providing new consent id when exchange token and most of the banks require
     * passing consent id which was already created.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if agent should generate new consent id in exchange token
     */
    public boolean shouldGenerateNewConsentIdInExchangeToken() {
        return shouldGenerateNewConsentIdInExchangeToken;
    }

    /**
     * Some banks do not allow to sent authorization header and token in body in both refresh and
     * exchange token request. For refresh and exchange token - refresh_token and exchange_token
     * fields must be used.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if we should send authorization header in exchange token request
     */
    public boolean shouldSentTokenInRefreshAndExchangeToken() {
        return shouldSentTokenInRefreshAndExchangeToken;
    }

    /**
     * Some banks do not like companyContext header when fetching transactions. When flag is set to
     * true - then companyContext header will be sent in transactions.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if we should companyContext in transactions
     */
    public boolean shouldSentCompanyContextInTransactions() {
        return shouldSentCompanyContextInTransactions;
    }

    /**
     * Some banks require providing pageId in the first request with pageId = 0. If this flag is set
     * to true - then such pageId will be added.
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if we should sent pageId = 0 in first transaction request.
     */
    public boolean shouldSentPageIdInFirstRequestAs0() {
        return shouldSentPageIdInFirstRequestAs0;
    }

    /**
     * In general, banks do not want to set from & to dates when fetching pending transactions. But
     * some of them want to do that - if this flag is enabled, we will only fetch pending
     * transactions for the next one month starting from "now".
     *
     * <p>USED ONLY IN POST API!!!
     *
     * @return Information if we should send dates in pending transactions.
     */
    public boolean shouldSendDatesInPendingTransactions() {
        return shouldSendDatesInPendingTransactions;
    }
}
