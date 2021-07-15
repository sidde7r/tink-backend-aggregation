package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents;

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
     * Return configuration for Bank - currently it seems that all banks has the same configuration
     * - but it may change in any cases please extend the object.
     */
    PolishApiConfiguration getApiConfiguration();

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
     * All of banks requires signing requests and sending information either in X-JWS-SIGNATURE or
     * JWS-SIGNATURE header. There are banks which needs to sent only signed payload and there are
     * some that needs list of headers and URI.
     *
     * @see PolishApiConstants.JwsHeaders#JWS_HEADERS
     * @see PolishApiSignatureFilter
     * @return information if we should attach headers and uri in signature
     */
    boolean shouldAttachHeadersAndUriInJws();

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
     * Some of banks supports exchange token flow - which allows us to change token with
     * AIS_ACCOUNTS consent to AIS. This operation is not reversible - so for banks which supports
     * the flow we are fetching accounts after auth part and saves them to persistent storage.
     *
     * @see PolishApiAuthenticator#exchangeAuthorizationCode(java.lang.String)
     * @return Information if bank supports exchange token flow
     */
    boolean doesSupportExchangeToken();

    /**
     * Account type is set based on accountType.code or accountType.description
     *
     * @see AccountTypeEntity
     */
    AccountTypeMapper getAccountTypeMapper();
}
