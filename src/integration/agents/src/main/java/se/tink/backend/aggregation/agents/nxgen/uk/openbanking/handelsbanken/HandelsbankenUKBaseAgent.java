package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.HandelsbankenConstants.Time.DEFAULT_ZONE_ID;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenOAuth2Authenticator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts.HandelsbankenUkAccountConverter;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants.Context;

@Slf4j
public class HandelsbankenUKBaseAgent extends HandelsbankenBaseAgent {

    private final HandelsbankenUkAccountConverter accountConverter;
    private final AgentComponentProvider componentProvider;

    public HandelsbankenUKBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.componentProvider = componentProvider;
        this.accountConverter = new HandelsbankenUkAccountConverter();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    // TODO: IFD-3379 Remove whole method when all UK customers have been migrated to UK certs
    @Override
    protected EidasIdentity getEidasIdentity() {

        try {

            Toggle toggle =
                    Toggle.of("uk-handelsbanken-use-uk-certs")
                            .context(
                                    UnleashContext.builder()
                                            .addProperty(
                                                    Context.PROVIDER_NAME.getValue(),
                                                    context.getProviderId())
                                            .addProperty(
                                                    Context.APP_ID.getValue(), context.getAppId())
                                            .build())
                            .build();

            UnleashClient unleashClient = componentProvider.getUnleashClient();
            if (unleashClient.isToggleEnabled(toggle)) {

                log.info(
                        "[HandelsbankenUKBaseAgent] uk-handelsbanken-use-uk-certs toggle enabled"
                                + " [certId: `{}`, appId: `{}`, providerName: `{}`]",
                        context.getCertId(),
                        context.getAppId(),
                        context.getProviderId());

                return new EidasIdentity(
                        context.getClusterId(),
                        context.getAppId(),
                        context.getCertId(),
                        context.getProviderId(),
                        getAgentClass());
            }
        } catch (Exception e) {
            log.warn("Something went wrong during uk-handelsbanken-use-uk-certs toggle switch");
        }

        log.info("[HandelsbankenUKBaseAgent] uk-handelsbanken-use-uk-certs toggle disabled");

        return new EidasIdentity(
                this.context.getClusterId(),
                this.context.getAppId(),
                HandelsbankenConstants.CERT_ID,
                HandelsbankenConstants.PROVIDER_NAME,
                getAgentClass());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new HandelsbankenOAuth2Authenticator(
                                apiClient, agentConfiguration, persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    protected LocalDate getMaxPeriodTransactions() {
        return localDateTimeSource
                .now(DEFAULT_ZONE_ID)
                .minusMonths(HandelsbankenConstants.MAX_FETCH_PERIOD_MONTHS)
                .plusDays(2)
                .toLocalDate();
    }

    @Override
    protected String getMarket() {
        return Market.GREAT_BRITAIN;
    }
}
