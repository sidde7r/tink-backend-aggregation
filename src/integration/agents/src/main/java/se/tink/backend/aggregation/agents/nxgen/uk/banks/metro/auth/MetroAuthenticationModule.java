package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication.AutoAuthenticationCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication.AutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.ConfirmChallengeCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.authentication.AuthenticationChallengeStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.deviceregistration.DeviceRegistrationChallengeStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition.FetchSeedPositionCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition.FetchSeedPositionStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.getcredentials.CredentialsGetStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.initial.InitialStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.logindevice.LoginDeviceCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.logindevice.LoginDeviceStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification.OtpVerificationCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification.OtpVerificationStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice.RegisterDeviceCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice.RegisterDeviceStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice.TulipReferenceCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.savecredentials.CredentialsSaveStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class MetroAuthenticationModule extends AbstractModule {

    @Singleton
    @Inject
    @Provides
    public AgentAbstractMultiStepsAuthenticationProcess metroAuthenticationProcessFacade(
            AgentComponentProvider componentProvider,
            ObjectMapperFactory objectMapperFactory,
            AgentPlatformHttpClient httpClient) {
        ObjectMapper objectMapper = objectMapperFactory.getInstance();
        MetroDataAccessorFactory metroDataAccessorFactory =
                new MetroDataAccessorFactory(objectMapper);
        List<AgentAuthenticationProcessStep<?>> steps =
                Arrays.asList(
                        new RegisterDeviceStep(
                                metroDataAccessorFactory,
                                new RegisterDeviceCall(httpClient),
                                new TulipReferenceCall(httpClient),
                                componentProvider.getRandomValueGenerator()),
                        new CredentialsGetStep(),
                        new CredentialsSaveStep(metroDataAccessorFactory),
                        new FetchSeedPositionStep(
                                metroDataAccessorFactory,
                                new FetchSeedPositionCall(httpClient, objectMapper)),
                        new OtpVerificationStep(
                                metroDataAccessorFactory,
                                new OtpVerificationCall(httpClient, objectMapper)),
                        new DeviceRegistrationChallengeStep(
                                metroDataAccessorFactory,
                                new ConfirmChallengeCall(httpClient, objectMapper)),
                        new LoginDeviceStep(
                                metroDataAccessorFactory, new LoginDeviceCall(httpClient)),
                        new AutoAuthenticationStep(
                                metroDataAccessorFactory, new AutoAuthenticationCall(httpClient)),
                        new AuthenticationChallengeStep(
                                metroDataAccessorFactory,
                                new ConfirmChallengeCall(httpClient, objectMapper)));

        return new MetroAuthenticationProcessFacade(
                steps, new InitialStep(metroDataAccessorFactory));
    }
}
