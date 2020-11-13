package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.DanskeBankBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.DanskeBankExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.DanskeBankSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer.DanskeBankSETransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.fetcher.transferdestinations.DanskeBankSETransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    LOANS,
    PAYMENTS,
    TRANSFERS
})
public final class DanskeBankSEAgent extends DanskeBankAgent
        implements RefreshTransferDestinationExecutor, RefreshIdentityDataExecutor {

    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public DanskeBankSEAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new DanskeBankSEConfiguration(),
                new AccountEntityMapper(MarketCode.SE.name()));
        configureHttpClient(client);
        transferDestinationRefreshController = constructTransferDestinationController();
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankSEApiClient(
                client, (DanskeBankSEConfiguration) configuration, credentials, catalog);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent("Mobilbank/813854 CFNetwork/808.2.16 Darwin/16.3.0");
        client.setDebugOutput(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(
                        supplementalRequester,
                        new DanskeBankBankIdAuthenticator(
                                (DanskeBankSEApiClient) apiClient,
                                deviceId,
                                configuration,
                                credentials,
                                sessionStorage),
                        persistentStorage,
                        credentials),
                new PasswordAuthenticationController(
                        new DanskeBankPasswordAuthenticator(
                                apiClient, deviceId, configuration, credentials)));
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        DanskeBankExecutorHelper executorHelper =
                new DanskeBankExecutorHelper(
                        (DanskeBankSEApiClient) apiClient,
                        deviceId,
                        configuration,
                        supplementalRequester);
        DanskeBankSETransferExecutor transferExecutor =
                new DanskeBankSETransferExecutor(
                        (DanskeBankSEApiClient) apiClient, configuration, executorHelper, catalog);
        DanskeBankSEPaymentExecutor paymentExecutor =
                new DanskeBankSEPaymentExecutor(
                        (DanskeBankSEApiClient) apiClient, configuration, executorHelper);

        return Optional.of(new TransferController(paymentExecutor, transferExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new DanskeBankSETransferDestinationFetcher());
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return sessionStorage
                .get(Storage.IDENTITY_INFO, FinalizeAuthenticationResponse.class)
                .map(
                        user ->
                                SeIdentityData.of(
                                        user.getUserInfo().getFirstName(),
                                        user.getUserInfo().getLastname(),
                                        user.getUserId()))
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
