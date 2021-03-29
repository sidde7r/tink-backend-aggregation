package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.firstdirect;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.PIS_UK_FASTER_PAYMENT;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.RequiredReferenceRemittanceInfoDomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.RequiredReferenceRemittanceInfoDomesticSchedulerPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.PaymentRequestWithRequiredReferenceRemittanceInfoValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.UkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.HsbcGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.signature.HsbcSignatureCreator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentDependencyModulesForProductionMode(modules = {UkOpenBankingModule.class})
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS})
@AgentPisCapability(capabilities = PIS_UK_FASTER_PAYMENT, markets = "GB")
public final class FirstDirectV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private static final UkOpenBankingPisConfiguration pisConfig;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
                        .withOrganisationId(FirstDirectConstants.ORGANISATION_ID)
                        .withApiBaseURL(FirstDirectConstants.AIS_API_URL)
                        .withWellKnownURL(FirstDirectConstants.WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.ACCOUNT_ID_PARTY)
                        .build();
        pisConfig =
                new UkOpenBankingPisConfiguration(
                        FirstDirectConstants.PIS_API_URL, FirstDirectConstants.WELL_KNOWN_URL);
    }

    @Inject
    public FirstDirectV31Agent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                pisConfig,
                createPisRequestFilter(
                        new HsbcSignatureCreator(jwtSigner),
                        jwtSigner,
                        componentProvider.getRandomValueGenerator()));
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration) {

        // FD is hsbc subsidiary so need to have the same overridden client
        return new HsbcGroupApiClient(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig);
    }

    @Override
    protected UkOpenBankingPaymentRequestValidator getPaymentRequestValidator() {
        return new PaymentRequestWithRequiredReferenceRemittanceInfoValidator();
    }

    @Override
    protected DomesticPaymentConverter getDomesticPaymentConverter() {
        return new RequiredReferenceRemittanceInfoDomesticPaymentConverter();
    }

    @Override
    protected DomesticScheduledPaymentConverter getDomesticScheduledPaymentConverter() {
        return new RequiredReferenceRemittanceInfoDomesticSchedulerPaymentConverter();
    }
}
