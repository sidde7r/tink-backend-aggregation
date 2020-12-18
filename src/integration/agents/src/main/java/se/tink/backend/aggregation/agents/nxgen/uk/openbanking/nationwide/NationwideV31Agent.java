package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.JwtSignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256WithoutBase64SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingRs256SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.pis.filter.NationwidePisRequestFilter;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModulesForProductionMode(
        modules = {UkOpenBankingModule.class, JwtSignerModule.class})
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS})
public final class NationwideV31Agent extends UkOpenBankingBaseAgent {

    private static final se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking
                    .ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig
            aisConfig;
    private final LocalDateTimeSource localDateTimeSource;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
                        .withOrganisationId(NationwideConstants.ORGANISATION_ID)
                        .withApiBaseURL(NationwideConstants.AIS_API_URL)
                        .withWellKnownURL(NationwideConstants.WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES)
                        .build();
    }

    @Inject
    public NationwideV31Agent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                new UkOpenBankingPisConfiguration(
                        NationwideConstants.PIS_API_URL, NationwideConstants.WELL_KNOWN_URL),
                createPisRequestFilter(jwtSigner, componentProvider.getRandomValueGenerator()));
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
    }

    @Override
    protected UkOpenBankingAis makeAis() {

        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();

        CreditCardAccountMapper creditCardAccountMapper =
                new CreditCardAccountMapper(
                        new NationwideCreditCardBalanceMapper(
                                new DefaultCreditCardBalanceMapper(valueExtractor)),
                        new DefaultIdentifierMapper(valueExtractor));
        return new UkOpenBankingV31Ais(
                aisConfig, persistentStorage, creditCardAccountMapper, localDateTimeSource);
    }

    private static NationwidePisRequestFilter createPisRequestFilter(
            JwtSigner jwtSigner, RandomValueGenerator randomValueGenerator) {
        final UkOpenBankingPaymentStorage paymentStorage = new UkOpenBankingPaymentStorage();
        final UkOpenBankingPs256SignatureCreator ps256SignatureCreator =
                new UkOpenBankingPs256WithoutBase64SignatureCreator(jwtSigner);
        final UkOpenBankingJwtSignatureHelper jwtSignatureHelper =
                new UkOpenBankingJwtSignatureHelper(
                        new ObjectMapper(),
                        paymentStorage,
                        new UkOpenBankingRs256SignatureCreator(jwtSigner),
                        ps256SignatureCreator);

        return new NationwidePisRequestFilter(
                jwtSignatureHelper, paymentStorage, randomValueGenerator);
    }
}
