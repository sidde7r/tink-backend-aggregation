package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankDecoupledPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentRedirectOauthHelper;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = {PisCapability.SEPA_CREDIT_TRANSFER})
public final class CommerzBankAgent extends Xs2aDevelopersTransactionalAgent
        implements RefreshTransferDestinationExecutor {

    @Inject
    public CommerzBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://psd2.api.commerzbank.com");
        client.addFilter(new TimeoutFilter());
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
        return new CommerzBankApiClient(
                client,
                persistentStorage,
                configuration,
                componentProvider.getUser().isPresent(),
                userIp,
                componentProvider.getRandomValueGenerator(),
                componentProvider.getContext().getLogMasker());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
