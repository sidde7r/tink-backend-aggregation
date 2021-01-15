package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Locale;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClientProvider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.fetchers.transactional.SwedbankSEBusinessTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.profile.SwedbankBusinessProfileSelector;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class SwedbankSEBusinessAgent extends SwedbankAbstractAgent {

    @Inject
    public SwedbankSEBusinessAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new SwedbankConfiguration(
                        SwedbankSEConstants.PROFILE_PARAMETERS.get(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getProvider()
                                        .getPayload()),
                        SwedbankSEConstants.HOST,
                        false),
                new SwedbankSEApiClientProvider(
                        new SwedbankBusinessProfileSelector(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getCredentials()
                                        .getField(Key.CORPORATE_ID))),
                new SwedbankDateUtils(ZoneId.of("Europe/Stockholm"), new Locale("sv", "SE")));
    }

    @Override
    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        SwedbankDefaultTransactionalAccountFetcher transactionalFetcher =
                new SwedbankSEBusinessTransactionalAccountFetcher(apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher),
                        transactionalFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalFetcher,
                transactionFetcherController);
    }
}
