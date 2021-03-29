package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public abstract class RevolutBaseAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final LocalDateTimeSource localDateTimeSource;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
                        .withOrganisationId(RevolutConstants.ORGANISATION_ID)
                        .withApiBaseURL(RevolutConstants.AIS_API_URL)
                        .withWellKnownURL(RevolutConstants.WELL_KNOWN_URL)
                        .build();
    }

    public RevolutBaseAgent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                new UkOpenBankingPisConfiguration(
                        RevolutConstants.PIS_API_URL, RevolutConstants.WELL_KNOWN_URL),
                createPisRequestFilterUsingPs256WithoutBase64Signature(
                        jwtSigner, componentProvider.getRandomValueGenerator()));
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
    }

    @Override
    protected TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new UkOpenBankingTransferDestinationFetcher(
                        new RevolutTransferDestinationAccountsProvider(apiClient),
                        AccountIdentifierType.IBAN,
                        IbanIdentifier.class));
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        DefaultIdentifierMapper identifierMapper = new DefaultIdentifierMapper(valueExtractor);

        return new UkOpenBankingV31Ais(
                aisConfig,
                persistentStorage,
                localDateTimeSource,
                new CreditCardAccountMapper(
                        new DefaultCreditCardBalanceMapper(valueExtractor), identifierMapper),
                new RevolutTransactionalAccountMapperDecorator(
                        new RevolutTransactionalAccountMapper(
                                new TransactionalAccountBalanceMapper(valueExtractor),
                                identifierMapper)));
    }

    @Override
    protected Class<? extends UkOpenBankingClientConfigurationAdapter>
            getClientConfigurationFormat() {
        throw new IllegalStateException("This method need to be overridden in final class!");
    }
}
