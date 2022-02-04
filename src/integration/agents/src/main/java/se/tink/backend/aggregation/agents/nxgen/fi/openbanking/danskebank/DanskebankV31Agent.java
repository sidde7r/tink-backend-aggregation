package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.fetcher.DanskeBankFITransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.mapper.DanskeFiIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.rcp.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskeBankV31EUBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskeBankPisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer.DanskeOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter.UkOpenBankingPisRequestFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256MinimalSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingRs256SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModulesForProductionMode(modules = DanskeOpenBankingModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, TRANSFERS})
@AgentPisCapability(capabilities = {PisCapability.SEPA_CREDIT_TRANSFER})
public final class DanskebankV31Agent extends DanskeBankV31EUBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    static {
        aisConfig =
                new DanskebankAisConfiguration.Builder(V31.AIS_BASE, MarketCode.FI)
                        .withWellKnownURL(V31.getWellKnownUrl(MarketCode.FI))
                        .build();
    }

    @Inject
    public DanskebankV31Agent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(
                componentProvider,
                flowFacade,
                aisConfig,
                new DanskeBankPisConfiguration.Builder(
                                V31.getPisBaseUrl().toString(), MarketCode.FI)
                        .withWellKnownURL(V31.getWellKnownUrl(MarketCode.FI))
                        .build(),
                createPisRequestFilter(
                        new UkOpenBankingPs256MinimalSignatureCreator(flowFacade.getJwtSinger()),
                        flowFacade.getJwtSinger(),
                        componentProvider.getRandomValueGenerator()),
                creditCardAccountMapper());
        this.transferDestinationRefreshController = constructTransferDestinationController();
        client.addFilter(new TerminatedHandshakeRetryFilter());
    }

    private static CreditCardAccountMapper creditCardAccountMapper() {
        PrioritizedValueExtractor prioritizedValueExtractor = new PrioritizedValueExtractor();
        return new CreditCardAccountMapper(
                getCreditCardBalanceMapper(prioritizedValueExtractor),
                new DanskeFiIdentifierMapper(prioritizedValueExtractor));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        try {
            return super.fetchCheckingAccounts();
        } catch (HttpResponseException e) {
            int errorCode = e.getResponse().getStatus();
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if (errorCode == 500) {
                if (!errorResponse.getErrors().isEmpty()
                        && ErrorCode.UNEXPETED_ERROR.equals(
                                errorResponse.getErrors().get(0).getErrorCode())) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                }
            } else if (errorCode == 403
                    && errorResponse
                            .getMessage()
                            .equals(
                                    "The PSU does not have access to the requested account or it doesn't exist.")) {
                throw SessionError.CONSENT_INVALID.exception();
            }
            throw e;
        }
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new DanskeBankFITransferDestinationFetcher());
    }

    protected static UkOpenBankingPisRequestFilter createPisRequestFilter(
            UkOpenBankingPs256SignatureCreator ps256SignatureCreator,
            JwtSigner jwtSigner,
            RandomValueGenerator randomValueGenerator) {
        final UkOpenBankingPaymentStorage paymentStorage = new UkOpenBankingPaymentStorage();
        final UkOpenBankingJwtSignatureHelper jwtSignatureHelper =
                new UkOpenBankingJwtSignatureHelper(
                        new ObjectMapper(),
                        paymentStorage,
                        new UkOpenBankingRs256SignatureCreator(jwtSigner),
                        ps256SignatureCreator);

        return new UkOpenBankingPisRequestFilter(
                jwtSignatureHelper, paymentStorage, randomValueGenerator);
    }
}
