package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.SUPPORTED_TRANSACTION_TYPES;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiPostAuthorizationClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
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
     * Account type is set based on accountType.code or accountType.description
     *
     * @see AccountTypeEntity
     */
    AccountTypeMapper getAccountTypeMapper();

    /**
     * PolishApiLogicFlowConfigurator contains all flags with differences in the flows - like
     * authorization, obtaining token, fetching accounts and transactions.
     *
     * @return object with configuration for authorization.
     */
    PolishApiLogicFlowConfigurator getLogicFlowConfigurator();

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
