package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.SUPPORTED_TRANSACTION_TYPES;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiPostAuthorizationClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature.PolishApiSignatureFilter;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

public interface PolishApiAgentCreator {

    /**
     * Banks may have one of to API approaches - one with the GET (currently MBank) and POST (other
     * banks)
     *
     * @see
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetAccountsApiUrlFactory
     * @see
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostAccountsApiUrlFactory
     * @return proper factory depending on the case
     */
    PolishAccountsApiUrlFactory getAccountApiUrlFactory();

    /**
     * Banks may have one of to API approaches - one with the GET (currently MBank) and POST (other
     * banks). Please note that even MBank uses post request for getting authorize code and token
     * exchanges.
     *
     * @see
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetAuthorizeApiUrlFactory
     * @see
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostAuthorizeApiUrlFactory
     * @return proper factory depending on the case
     */
    PolishAuthorizeApiUrlFactory getAuthorizeApiUrlFactory();

    /**
     * Banks may have one of to API approaches - one with the GET (currently MBank) and POST (other
     * banks)
     *
     * @see
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetTransactionsApiUrlFactory
     * @see
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostTransactionsApiUrlFactory
     * @return proper factory depending on the case
     */
    PolishTransactionsApiUrlFactory getTransactionsApiUrlFactory();

    /**
     * Different banks support different number of days for transaction history set this value
     * properly. This value is also passed when creating consent.
     *
     * @see PolishApiPostAuthorizationClient#prepareAisPrivilegeList() as example.
     * @return max days to fetch in transactions.
     */
    int getMaxDaysToFetch();

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
    boolean shouldGetAccountListFromTokenResponse();

    /**
     * Account type is set based on accountType.code or accountType.description
     *
     * @see AccountTypeEntity
     */
    AccountTypeMapper getAccountTypeMapper();

    /**
     * All of banks requires signing requests and sending information either in X-JWS-SIGNATURE or
     * JWS-SIGNATURE header. There are banks which needs to sent only signed payload and there are
     * some that needs list of headers and URI.
     *
     * @see PolishApiConstants.JwsHeaders#JWS_HEADERS
     * @see PolishApiSignatureFilter
     * @return information if we should attach headers and uri in signature
     */
    default boolean shouldAttachHeadersAndUriInJws() {
        return false;
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
    default boolean shouldSentAuthorizationModeInTokenRequest() {
        return false;
    }

    /**
     * Currently only BNP Paribas needs to have clientId sent in requestHeader body.
     *
     * @return information if we should attach client id in the requestHeader entity in the
     *     requests.
     */
    default boolean shouldSentClientIdInRequestHeaderBody() {
        return false;
    }

    /**
     * Some of banks supports exchange token flow - which allows us to change token with
     * AIS_ACCOUNTS consent to AIS. This operation is not reversible - so for banks which supports
     * the flow we are fetching accounts after auth part and saves them to persistent storage.
     *
     * @see PolishApiAuthenticator#exchangeAuthorizationCode(java.lang.String)
     * @return Information if bank supports exchange token flow
     */
    default boolean doesSupportExchangeToken() {
        return true;
    }

    /**
     * Some of the banks requires providing "Bearer" phrase before token in the body. When this flag
     * is set to true then Bearer is added.
     *
     * @return Information if bank needs Bearer in the requestHeader.token request.
     */
    default boolean shouldAddBearerStringInTokenInRequestBody() {
        return false;
    }

    /**
     * Some of the banks requires providing single scope in the authorize request in ais accounts
     * and some are fine with multiple.
     *
     * @return Information if should sent single in scope limits
     */
    default boolean shouldSentSingleScopeLimitInAisAccounts() {
        return false;
    }

    /**
     * Some banks allows mixings ais and ais-accounts in privileges lists. For that case ais scope
     * is sent.
     *
     * @return Information if bank allows to mix ais and ais-accounts scopes
     */
    default boolean canCombineAisAndAisAccountsScopes() {
        return false;
    }

    /**
     * Some banks allows to fetch transactions using params: transactionDateFrom, transactionDateTo
     * and some uses bookingDateFrom, bookingDateTo
     *
     * <p>If your bank does not support transactionDateFrom(To) return false.
     *
     * @return Information if bank allows to mix ais and ais-accounts scopes
     */
    default boolean doesSupportTransactionDateFrom() {
        return true;
    }

    /**
     * In general PolishAPI standard - handles authorization_code in TokenRequest in Code field.
     * However some of the banks changed Code to code.
     *
     * <p>If code is needed in lowercase set this value to false.
     *
     * @return Information information if code needs to be sent in UpperCase manner.
     */
    default boolean shouldSentAuthorizationCodeInUpperCaseField() {
        return true;
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
    default boolean shouldSentScopeAndScopeDetailsInFirstTokenRequest() {
        return true;
    }

    /**
     * Tink currently supports done and pending transactions. However polish API returns also
     * SCHEDULED, REJECTED, HOLD, CANCELLED transaction types.
     *
     * <p>Also there is situation that Bank which implements polish API does not support pending
     * transactions. In that case please override the method and return only DONE in List of
     * supported Transaction types.
     *
     * @return
     */
    default List<PolishApiConstants.Transactions.TransactionTypeRequest>
            getSupportedTransactionTypes() {
        return SUPPORTED_TRANSACTION_TYPES;
    }

    /**
     * Return configuration for Bank - currently it seems that all banks has the same configuration
     * - but it may change in any cases please extend the object.
     */
    default PolishApiConfiguration getApiConfiguration() {
        return new PolishApiConfiguration();
    }
}
