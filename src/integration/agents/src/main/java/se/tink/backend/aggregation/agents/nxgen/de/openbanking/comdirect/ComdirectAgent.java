package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect.filter.ComdirectRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankDecoupledPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentRedirectOauthHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = {PisCapability.SEPA_CREDIT_TRANSFER})
public final class ComdirectAgent extends Xs2aDevelopersAgent
        implements RefreshTransferDestinationExecutor {

    @Inject
    public ComdirectAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://xs2a-api.comdirect.de");
        addFilters(client);
    }

    private void addFilters(TinkHttpClient client) {
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.addFilter(new BadGatewayFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ComdirectRetryFilter(5, 1000));
        client.addFilter(new TerminatedHandshakeRetryFilter());
    }

    @Override
    protected TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider agentComponentProvider) {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, null);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new ParallelTransactionFetcher<>(
                        apiClient, agentComponentProvider.getLocalDateTimeSource()));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        OAuth2AuthenticationController redirectPaymentAuthenticator =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new Xs2aDevelopersPaymentRedirectOauthHelper(
                                apiClient, persistentStorage, sessionStorage, configuration),
                        credentials,
                        strongAuthenticationState);

        CommerzBankDecoupledPaymentAuthenticator decoupledPaymentAuthenticator =
                new CommerzBankDecoupledPaymentAuthenticator(
                        (CommerzBankApiClient) apiClient,
                        sessionStorage,
                        supplementalInformationController,
                        supplementalInformationFormer);

        CommerzBankPaymentAuthenticator authenticator =
                new CommerzBankPaymentAuthenticator(
                        credentials,
                        persistentStorage,
                        sessionStorage,
                        new ThirdPartyAppAuthenticationController<>(
                                redirectPaymentAuthenticator, supplementalInformationHelper),
                        decoupledPaymentAuthenticator);

        CommerzBankPaymentExecutor commerzBankPaymentExecutor =
                new CommerzBankPaymentExecutor(
                        (CommerzBankApiClient) apiClient,
                        authenticator,
                        sessionStorage,
                        credentials);

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(commerzBankPaymentExecutor)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
    }

    @Override
    protected Xs2aDevelopersApiClient constructApiClient(AgentComponentProvider componentProvider) {
        User user = componentProvider.getUser();
        return new CommerzBankApiClient(
                client,
                persistentStorage,
                configuration,
                user.isPresent(),
                user.getIpAddress(),
                componentProvider.getRandomValueGenerator(),
                componentProvider.getContext().getLogMasker());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
