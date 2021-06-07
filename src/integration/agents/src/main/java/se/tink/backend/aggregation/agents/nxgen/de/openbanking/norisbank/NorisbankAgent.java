package se.tink.backend.aggregation.agents.nxgen.de.openbanking.norisbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.executor.payment.DeutscheBankPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.executor.payment.DeutscheBankPaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.RedirectPaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public final class NorisbankAgent extends DeutscheBankAgent
        implements RefreshTransferDestinationExecutor {
    private static final DeutscheMarketConfiguration NORIS_CONFIGURATION =
            new DeutscheMarketConfiguration(
                    "https://xs2a.db.com/{" + Parameters.SERVICE_KEY + "}/DE/Noris",
                    "DE_ONLB_NORIS");

    @Inject
    public NorisbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new NorisbankApiClient(client, persistentStorage, headerValues, NORIS_CONFIGURATION);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        RedirectPaymentAuthenticator redirectPaymentAuthenticator =
                new RedirectPaymentAuthenticator(
                        supplementalInformationController, strongAuthenticationState);

        DeutscheBankPaymentApiClient apiClient =
                new DeutscheBankPaymentApiClient(
                        client,
                        persistentStorage,
                        NORIS_CONFIGURATION,
                        headerValues,
                        credentials,
                        strongAuthenticationState,
                        randomValueGenerator,
                        new DeutscheBankPaymentMapper());

        BasePaymentExecutor paymentExecutor =
                new BasePaymentExecutor(apiClient, redirectPaymentAuthenticator, sessionStorage);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
