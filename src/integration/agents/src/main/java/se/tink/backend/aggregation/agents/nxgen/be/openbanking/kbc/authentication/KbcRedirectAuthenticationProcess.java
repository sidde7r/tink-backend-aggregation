package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.IbanAgentUserInteractionAuthenticationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationInitialProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchRefreshableAccessTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;

public class KbcRedirectAuthenticationProcess extends RedirectAuthenticationProcess {

    private final KbcFetchConsentAuthenticationStep fetchConsentAuthenticationStep;
    private final KbcConsentValidationStep consentValidationStep;

    public KbcRedirectAuthenticationProcess(
            RedirectAuthenticationInitialProcessStep redirectAuthenticationInitialProcessStep,
            RedirectAuthenticationAccessTokenValidationStep accessTokenValidationStep,
            RedirectAuthenticationRefreshTokenStep refreshTokenStep,
            RedirectPreparationRedirectUrlStep redirectPreparationRedirectUrlStep,
            RedirectFetchRefreshableAccessTokenStep redirectFetchAuthenticationTokensStep,
            KbcFetchConsentAuthenticationStep fetchConsentAuthenticationStep,
            KbcConsentValidationStep consentValidationStep) {
        super(
                redirectAuthenticationInitialProcessStep,
                accessTokenValidationStep,
                refreshTokenStep,
                redirectPreparationRedirectUrlStep,
                redirectFetchAuthenticationTokensStep);
        this.fetchConsentAuthenticationStep = fetchConsentAuthenticationStep;
        this.consentValidationStep = consentValidationStep;
    }

    @Override
    public void registerSteps() {
        super.registerSteps();
        addStep(
                new IbanAgentUserInteractionAuthenticationStep(
                        AgentAuthenticationProcessStep.identifier(
                                KbcFetchConsentAuthenticationStep.class)));
        addStep(fetchConsentAuthenticationStep);
        addStep(consentValidationStep);
    }
}
