package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ing;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.IngPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.IngPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.IngPaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.libraries.enums.MarketCode;

@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class IngAgent extends IngBaseAgent {

    private static final Pattern HOLDER_NAME_SPLITTER =
            Pattern.compile("[,;]", Pattern.CASE_INSENSITIVE);

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }

    @Override
    public LocalDate earliestTransactionHistoryDate() {
        // All transaction information since the payment account was opened
        return localDateTimeSource.now(ZoneId.of("CET")).toLocalDate().minusYears(7);
    }

    @Override
    public List<Party> convertHolderNamesToParties(String holderNames) {
        return HOLDER_NAME_SPLITTER
                .splitAsStream(holderNames)
                .map(String::trim)
                .map(name -> new Party(name, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        // NZG-964
        // This overwrites the super method just to add the exception handling and validation to
        // just our markets
        // This should be expanded to cover entire serviceprovider once the solution is validated,
        // and other teams are notified.
        IngPaymentAuthenticator paymentAuthenticator =
                new IngPaymentAuthenticator(supplementalInformationController);

        IngPaymentMapper paymentMapper = new IngPaymentMapper(new BasePaymentMapper());

        IngPaymentExecutor paymentExecutor =
                new IngPaymentExecutor(
                        sessionStorage,
                        paymentApiClient,
                        paymentAuthenticator,
                        paymentMapper,
                        this.getClass().getAnnotations());

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
}
