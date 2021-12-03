package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken.filters.AddBusinessFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class HandelsbankenSEBusinessAgent extends HandelsbankenBaseAgent
        implements RefreshTransferDestinationExecutor {

    private final HandelsbankenSeBusinessAccountConverter accountConverter;

    @Inject
    public HandelsbankenSEBusinessAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.accountConverter = new HandelsbankenSeBusinessAccountConverter();
        client.addFilter(
                new AddBusinessFilter(request.getCredentials().getField("psu-corporate-id")));

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    protected LocalDate getMaxPeriodTransactions() {
        return LocalDate.now()
                .minusMonths(HandelsbankenSEBusinessConstants.MAX_FETCH_PERIOD_MONTHS);
    }

    @Override
    protected String getMarket() {
        return Market.SWEDEN;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new HandelsbankenBankIdAuthenticator(apiClient, sessionStorage),
                        persistentStorage,
                        request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                bankIdAuthenticationController,
                bankIdAuthenticationController);
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts,
                AccountIdentifierType.SE,
                AccountIdentifierType.SE_PG,
                AccountIdentifierType.SE_BG);
    }
}
