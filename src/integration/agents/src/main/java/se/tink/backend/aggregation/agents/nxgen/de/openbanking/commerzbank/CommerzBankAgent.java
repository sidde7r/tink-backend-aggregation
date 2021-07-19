package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentExecutor;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class CommerzBankAgent extends Xs2aDevelopersTransactionalAgent
        implements RefreshTransferDestinationExecutor {

    @Inject
    public CommerzBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://psd2.api.commerzbank.com");
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        CommerzBankPaymentAuthenticator authenticator =
                new CommerzBankPaymentAuthenticator((CommerzBankApiClient) apiClient, credentials);

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new Xs2aDevelopersPaymentAuthenticator(
                                apiClient, persistentStorage, configuration),
                        credentials,
                        strongAuthenticationState);

        Xs2aDevelopersPaymentExecutor xs2aDevelopersPaymentExecutor =
                new CommerzBankPaymentExecutor(
                        apiClient,
                        new ThirdPartyAppAuthenticationController<>(
                                controller, supplementalInformationHelper),
                        credentials,
                        persistentStorage,
                        sessionStorage,
                        authenticator);

        return Optional.of(
                new PaymentController(
                        xs2aDevelopersPaymentExecutor, xs2aDevelopersPaymentExecutor));
    }

    @Override
    protected Xs2aDevelopersApiClient constructApiClient(AgentComponentProvider componentProvider) {
        return new CommerzBankApiClient(
                client,
                persistentStorage,
                configuration,
                request.getUserAvailability().isUserPresent(),
                userIp,
                componentProvider.getRandomValueGenerator());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
