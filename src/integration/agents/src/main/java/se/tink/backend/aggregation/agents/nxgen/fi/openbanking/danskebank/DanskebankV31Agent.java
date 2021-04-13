package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.mapper.DanskeFiIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.rcp.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskeBankV31EUBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer.DanskeOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModulesForProductionMode(modules = DanskeOpenBankingModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class DanskebankV31Agent extends DanskeBankV31EUBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                new DanskebankAisConfiguration.Builder(V31.AIS_BASE, MarketCode.FI)
                        .withWellKnownURL(V31.getWellKnownUrl(MarketCode.FI))
                        .build();
    }

    @Inject
    public DanskebankV31Agent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(componentProvider, flowFacade, aisConfig, creditCardAccountMapper());
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
            if (e.getResponse().getStatus() == 500) {
                ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
                if (!errorResponse.getErrors().isEmpty()
                        && ErrorCode.UNEXPETED_ERROR.equals(
                                errorResponse.getErrors().get(0).getErrorCode())) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                }
            }
            throw e;
        }
    }
}
