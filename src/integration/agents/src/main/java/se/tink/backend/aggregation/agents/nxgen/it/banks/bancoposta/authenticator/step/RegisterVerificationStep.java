package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse.Body.AccountDetails;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.common.rpc.SimpleRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
@Slf4j
public class RegisterVerificationStep implements AuthenticationStep {
    private final BancoPostaApiClient apiClient;
    private final BancoPostaStorage storage;
    private AuthenticationStepResponse nextStep;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request) {
        VerificationOnboardingResponse verificationOnboardingResponse =
                apiClient.verifyOnboarding(new SimpleRequest());

        // Developed with ambassador who has only one bank account. Can't predict behaviour when
        // user has
        // multiple accounts in bank (not multiple instrument acounts) ITE-1331
        List<AccountDetails> accountDetails =
                verificationOnboardingResponse.getBody().getAccountsDetails();

        accountDetails.stream()
                .filter(acc -> acc.isActive())
                .findFirst()
                .ifPresent(
                        acc ->
                                storage.saveToPersistentStorage(
                                        Storage.ACCOUNT_NUMBER, acc.getAccountNumber()));
        if (accountDetails.size() > 1) {
            log.warn("There are multiple accounts received, can be not supported by our auth flow");
        }
        syncWalletIfNeeded(verificationOnboardingResponse);
        onboardIfNeeded(verificationOnboardingResponse);
        registerAppIfPossible(verificationOnboardingResponse);

        if (this.nextStep != null) {
            return nextStep;
        }
        throw LoginError.NOT_SUPPORTED.exception(
                "Authentication flow is unknown and not supported");
    }

    private void registerAppIfPossible(VerificationOnboardingResponse response) {
        if (StringUtils.isNotBlank(response.getBody().getRegisterToken())) {
            this.nextStep =
                    AuthenticationStepResponse.executeStepWithId(
                            BancoPostaAuthenticator.REGISTER_APP_STEP_ID);
        }
    }

    private void onboardIfNeeded(VerificationOnboardingResponse response) {
        if (response.getBody().isOnboardingRequired()) {
            this.nextStep =
                    AuthenticationStepResponse.executeStepWithId(
                            BancoPostaAuthenticator.ONBOARDING_STEP_ID);
        }
    }

    private void syncWalletIfNeeded(VerificationOnboardingResponse response) {
        if (response.getBody().isSyncWalletRequired()) {
            this.nextStep =
                    AuthenticationStepResponse.executeStepWithId(
                            BancoPostaAuthenticator.SYNC_WALLET_STEP_ID);
        }
    }

    public String getIdentifier() {
        return BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID;
    }
}
