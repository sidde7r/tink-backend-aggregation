package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

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
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public final class DeutscheBankDEAgent extends DeutscheBankAgent
        implements RefreshTransferDestinationExecutor {

    private static final DeutscheMarketConfiguration DEUTSCHE_DE_CONFIGURATION =
            new DeutscheMarketConfiguration(
                    "https://xs2a.db.com/{" + Parameters.SERVICE_KEY + "}/DE/DB", "DE_ONLB_DB");

    @Inject
    public DeutscheBankDEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new DeutscheBankDEApiClient(
                client,
                persistentStorage,
                headerValues,
                DEUTSCHE_DE_CONFIGURATION,
                randomValueGenerator,
                localDateTimeSource);
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
                        DEUTSCHE_DE_CONFIGURATION,
                        headerValues,
                        credentials,
                        strongAuthenticationState,
                        randomValueGenerator,
                        new DeutscheBankPaymentMapper(),
                        localDateTimeSource);

        BasePaymentExecutor paymentExecutor =
                new BasePaymentExecutor(apiClient, redirectPaymentAuthenticator, sessionStorage);

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(paymentExecutor)
                        .fetchablePaymentExecutor(paymentExecutor)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
