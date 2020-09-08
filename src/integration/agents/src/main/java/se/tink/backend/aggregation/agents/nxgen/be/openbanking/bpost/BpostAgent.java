package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator.BPostAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public final class BpostAgent extends Xs2aDevelopersTransactionalAgent {

    protected BPostAuthenticator bPostAuthenticator;

    @Inject
    public BpostAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.psd2.bpostbank.be");
        bPostAuthenticator = new BPostAuthenticator(apiClient, persistentStorage, configuration);
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final AccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, oauth2Authenticator);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher(
                                apiClient, oauth2Authenticator));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        bPostAuthenticator,
                        credentials,
                        strongAuthenticationState,
                        request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }
}
