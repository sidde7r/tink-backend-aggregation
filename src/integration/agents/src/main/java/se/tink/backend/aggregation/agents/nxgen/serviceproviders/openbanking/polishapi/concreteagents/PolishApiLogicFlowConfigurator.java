package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature.PolishApiSignatureFilter;

@Builder
public class PolishApiLogicFlowConfigurator {

    // Flags are set to values comaptible with Polish API Standard
    // If your bank has some other logic just override needed flags.
    @Builder.Default private boolean shouldGetAccountListFromTokenResponse = false;
    @Builder.Default private boolean shouldAttachHeadersAndUriInJws = false;
    @Builder.Default private boolean shouldSentAuthorizationModeInTokenRequest = false;
    @Builder.Default private boolean shouldSentClientIdInRequestHeaderBody = false;
    @Builder.Default private boolean doesSupportExchangeToken = true;
    @Builder.Default private boolean shouldAddBearerStringInTokenInRequestBody = false;
    @Builder.Default private boolean shouldSentSingleScopeLimitInAisAccounts = false;
    @Builder.Default private boolean canCombineAisAndAisAccountsScopes = false;
    @Builder.Default private boolean doesSupportTransactionDateFrom = true;
    @Builder.Default private boolean shouldSentAuthorizationCodeInUpperCaseField = true;
    @Builder.Default private boolean shouldSentScopeAndScopeDetailsInFirstTokenRequest = true;
    @Builder.Default private boolean shouldGenerateNewConsentIdInExchangeToken = false;

    /**
     * All of banks in Token response returns list of accounts number. But some of them does not
     * support getting accounts from the accounts endpoint or does not provide necessary data to
     * fetch account details - therefore in account details fetch the numbers are fetched from
     * persistent storange instead of accounts endpoint.
     *
     * @see PolishApiAuthenticator#refreshAccessToken(java.lang.String)
     * @see PolishApiTransactionalAccountFetcher#fetchAccountDetailsFromListOfAccountNumbers()
     * @return information if bank returns list of accounts in token response
     */
    public boolean shouldGetAccountListFromTokenResponse() {
        return shouldGetAccountListFromTokenResponse;
    }

    /**
     * All of banks requires signing requests and sending information either in X-JWS-SIGNATURE or
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
     * Currently only Pekao SA support authorization mode field. When setting value of this field to
     * extended we are getting possibility to fetch transaction history longer than 90 days.
     *
     * <p>Possible values for that field are: extended, standard, refresh. In our code we will use
     * extended
     *
     * @return information if we should attach authorization mode in the request
     */
    public boolean shouldSentAuthorizationModeInTokenRequest() {
        return shouldSentAuthorizationModeInTokenRequest;
    }

    /**
     * Currently only BNP Paribas needs to have clientId sent in requestHeader body.
     *
     * @return information if we should attach client id in the requestHeader entity in the
     *     requests.
     */
    public boolean shouldSentClientIdInRequestHeaderBody() {
        return shouldSentClientIdInRequestHeaderBody;
    }

    /**
     * Some of banks supports exchange token flow - which allows us to change token with
     * AIS_ACCOUNTS consent to AIS. This operation is not reversible - so for banks which supports
     * the flow we are fetching accounts after auth part and saves them to persistent storage.
     *
     * @see PolishApiAuthenticator#exchangeAuthorizationCode(java.lang.String)
     * @return Information if bank supports exchange token flow
     */
    public boolean doesSupportExchangeToken() {
        return doesSupportExchangeToken;
    }

    /**
     * Some of the banks requires providing "Bearer" phrase before token in the body. When this flag
     * is set to true then Bearer is added.
     *
     * @return Information if bank needs Bearer in the requestHeader.token request.
     */
    public boolean shouldAddBearerStringInTokenInRequestBody() {
        return shouldAddBearerStringInTokenInRequestBody;
    }

    /**
     * Some of the banks requires providing single scope in the authorize request in ais accounts
     * and some are fine with multiple.
     *
     * @return Information if should sent single in scope limits
     */
    public boolean shouldSentSingleScopeLimitInAisAccounts() {
        return shouldSentSingleScopeLimitInAisAccounts;
    }

    /**
     * Some banks allows mixing ais and ais-accounts in privileges lists. For that case ais scope is
     * sent.
     *
     * @return Information if bank allows to mix ais and ais-accounts scopes
     */
    public boolean canCombineAisAndAisAccountsScopes() {
        return canCombineAisAndAisAccountsScopes;
    }

    /**
     * Some banks allows to fetch transactions using params: transactionDateFrom, transactionDateTo
     * and some uses bookingDateFrom, bookingDateTo
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
     * Scope and scope details are first defined in AuthorizeRequest and some of the banks requires
     * to copy them in TokenRequest and some of them prohibits that - in general documentations of
     * banks might be misleading - because they state that the fields are required.
     *
     * <p>If your bank returns information that scope or scope details are invalid set this value to
     * false.
     *
     * @return Information if agent should sent scope and scope details in first token request.
     */
    public boolean shouldSentScopeAndScopeDetailsInFirstTokenRequest() {
        return shouldSentScopeAndScopeDetailsInFirstTokenRequest;
    }

    /**
     * Due to the fact that polish api standard can be interpreted in many different ways some of
     * the banks require providing new consent id when exchange token and most of the banks require
     * passing consent id which was already created.
     *
     * @return Information if agent should generate new consent id in exchange token
     */
    public boolean shouldGenerateNewConsentIdInExchangeToken() {
        return shouldGenerateNewConsentIdInExchangeToken;
    }
}
