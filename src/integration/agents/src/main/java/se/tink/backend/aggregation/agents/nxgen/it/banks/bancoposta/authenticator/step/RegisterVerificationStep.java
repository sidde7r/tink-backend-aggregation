package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import com.amazonaws.util.StringUtils;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RequestBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@AllArgsConstructor
@Slf4j
public class RegisterVerificationStep implements AuthenticationStep {
    private BancoPostaApiClient apiClient;
    private UserContext userContext;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request) {
        VerificationOnboardingResponse verificationOnboardingResponse =
                apiClient.verifyOnboarding(new RequestBody(new HashMap<>()));

        // Developed with ambassador who has only one account. Can't predict behaviour when user has
        // multiple accounts
        verificationOnboardingResponse.getBody().getAccountsDetails().stream()
                .filter(acc -> acc.isActive())
                .findFirst()
                .ifPresent(acc -> userContext.setAccountNumber(acc.getAccountNumber()));

        if (verificationOnboardingResponse.getBody().getAccountsDetails().size() > 1) {
            log.warn("There are multiple accounts received, can be not supported by our auth flow");
        }

        if (verificationOnboardingResponse.getBody().isSyncWalletRequired()) {
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.SYNC_WALLET_STEP_ID);
        } else if (verificationOnboardingResponse.getBody().isOnboardingRequired()) {
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.ONBOARDING_STEP_ID);
        } else if (!StringUtils.isNullOrEmpty(
                verificationOnboardingResponse.getBody().getRegisterToken())) {
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.REGISTER_APP_STEP_ID);
        } else {
            throw LoginError.NOT_SUPPORTED.exception(
                    "Authentication flow is unknown and not supported");
        }
    }

    public String getIdentifier() {
        return BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID;
    }
}
