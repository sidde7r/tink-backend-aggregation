package se.tink.agent.runtime.authentication.processes.oauth2_decoupled_swedish_mobile_bankid;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.steps.Oauth2FetchAccessToken;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_swedish_mobile_bankid.Oauth2DecoupledSwedishMobileBankIdAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.steps.SwedishMobileBankIdOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2DecoupledSwedishMobileBankId;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;
import se.tink.agent.sdk.utils.Sleeper;

public class Oauth2DecoupledSwedishMobileBankIdAuthenticationProcess
        implements AuthenticationProcess<Oauth2DecoupledSwedishMobileBankIdAuthenticator> {

    private final Sleeper sleeper;

    public Oauth2DecoupledSwedishMobileBankIdAuthenticationProcess(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    @Override
    public Optional<Oauth2DecoupledSwedishMobileBankIdAuthenticator> tryInstantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateOauth2DecoupledSwedishMobileBankId.class)
                .map(AuthenticateOauth2DecoupledSwedishMobileBankId::authenticator);
    }

    @Override
    public InteractiveExecutionFlow<Void, ConsentLifetime> getNewConsentFlow(
            Oauth2DecoupledSwedishMobileBankIdAuthenticator authenticator) {
        return InteractiveExecutionFlow.<Void, ConsentLifetime>startStep(
                        new ThirdPartyAppInitStep(
                                authenticator, SwedishMobileBankIdOpenAppStep.class))
                .addStep(
                        new SwedishMobileBankIdOpenAppStep(
                                authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                this.sleeper,
                                SwedishMobileBankIdOpenAppStep.class,
                                Oauth2FetchAccessToken.class))
                .addStep(new Oauth2FetchAccessToken(authenticator))
                .build();
    }

    @Override
    public NonInteractiveExecutionFlow<Void, ConsentStatus> getUseExistingConsentFlow(
            Oauth2DecoupledSwedishMobileBankIdAuthenticator authenticator) {
        return NonInteractiveExecutionFlow.startStep(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
