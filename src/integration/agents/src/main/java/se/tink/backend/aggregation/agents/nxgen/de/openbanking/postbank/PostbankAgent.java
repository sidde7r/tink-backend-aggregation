package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

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
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankIconUrlMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankPaymentsEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto.JwtGenerator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto.PostbankJwtModule;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.payment.PostbankPaymentMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.executor.payment.DeutscheBankPaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

@AgentDependencyModules(modules = PostbankJwtModule.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class PostbankAgent extends DeutscheBankAgent
        implements RefreshTransferDestinationExecutor {
    private static final DeutscheMarketConfiguration POSTBANK_CONFIGURATION =
            new DeutscheMarketConfiguration(
                    "https://xs2a.db.com/{" + Parameters.SERVICE_KEY + "}/DE/Postbank",
                    "DE_ONLB_POBA");
    private PostbankAuthenticator postbankAuthenticator;

    @Inject
    public PostbankAgent(AgentComponentProvider componentProvider, JwtGenerator jwtGenerator) {
        super(componentProvider);
        this.postbankAuthenticator =
                new PostbankAuthenticator(
                        (PostbankApiClient) apiClient, persistentStorage, credentials);
        ((PostbankApiClient) apiClient).enrichWithJwtGenerator(jwtGenerator);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new PostbankApiClient(
                client,
                persistentStorage,
                headerValues,
                POSTBANK_CONFIGURATION,
                randomValueGenerator,
                localDateTimeSource);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog,
                        supplementalInformationController,
                        postbankAuthenticator,
                        new PostbankEmbeddedFieldBuilder(catalog, new PostbankIconUrlMapper()),
                        randomValueGenerator);

        return new AutoAuthenticationController(
                request, context, postbankAuthenticationController, postbankAuthenticator);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        PostbankPaymentAuthenticator postbankPaymentAuthenticator =
                new PostbankPaymentAuthenticator(
                        catalog,
                        supplementalInformationController,
                        postbankAuthenticator,
                        credentials,
                        new PostbankPaymentsEmbeddedFieldBuilder(
                                catalog, new PostbankIconUrlMapper()),
                        randomValueGenerator);

        DeutscheBankPaymentApiClient apiClient =
                new DeutscheBankPaymentApiClient(
                        client,
                        persistentStorage,
                        POSTBANK_CONFIGURATION,
                        headerValues,
                        credentials,
                        strongAuthenticationState,
                        randomValueGenerator,
                        new PostbankPaymentMapper(),
                        localDateTimeSource);

        BasePaymentExecutor paymentExecutor =
                new BasePaymentExecutor(apiClient, postbankPaymentAuthenticator, sessionStorage);

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
