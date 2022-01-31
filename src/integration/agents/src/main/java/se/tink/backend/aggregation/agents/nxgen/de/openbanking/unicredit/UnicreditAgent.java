package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.detail.UnicreditEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.detail.UnicreditIconUrlMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.payment.UnicreditEmbeddedPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditApiClientRetryer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionsDateFromChooser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class UnicreditAgent extends UnicreditBaseAgent {

    private static final UnicreditProviderConfiguration PROVIDER_CONFIG =
            new UnicreditProviderConfiguration("HVB_ONLINEBANKING", "https://api.unicredit.de");

    @Inject
    public UnicreditAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, PROVIDER_CONFIG);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        UnicreditAuthenticator authenticator = constructUnicreditAuthenticator();

        return new AutoAuthenticationController(request, context, authenticator, authenticator);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        UnicreditAuthenticator authenticator = constructUnicreditAuthenticator();

        UnicreditEmbeddedPaymentExecutor paymentExecutor =
                new UnicreditEmbeddedPaymentExecutor(
                        apiClient, new UnicreditApiClientRetryer(), authenticator, sessionStorage);

        return Optional.of(
                new PaymentController(
                        paymentExecutor, paymentExecutor, new PaymentControllerExceptionMapper()));
    }

    private UnicreditAuthenticator constructUnicreditAuthenticator() {
        return new UnicreditAuthenticator(
                (UnicreditApiClient) apiClient,
                unicreditStorage,
                credentials,
                strongAuthenticationState,
                supplementalInformationController,
                new UnicreditEmbeddedFieldBuilder(catalog, new UnicreditIconUrlMapper()));
    }

    @Override
    protected UnicreditBaseApiClient getApiClient(
            UnicreditProviderConfiguration providerConfiguration,
            UnicreditBaseHeaderValues headerValues) {
        return new UnicreditApiClient(
                client,
                unicreditStorage,
                providerConfiguration,
                headerValues,
                randomValueGenerator,
                sessionStorage);
    }

    @Override
    protected UnicreditTransactionsDateFromChooser getUnicreditTransactionsDateFromChooser(
            LocalDateTimeSource localDateTimeSource) {
        return new HvbOnlineUnicreditTransactionsDateFromChooser(localDateTimeSource);
    }
}
