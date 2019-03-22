package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.rpc.PollResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class ErstebankSidentityAuthenticator implements Authenticator {

    private final ErsteBankApiClient ersteBankApiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public ErstebankSidentityAuthenticator(
            ErsteBankApiClient ersteBankApiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.ersteBankApiClient = ersteBankApiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getUsername();
        String verificationCode = ersteBankApiClient.getSidentityVerificationCode(username);
        supplementalInformationHelper.waitAndShowLoginDescription(verificationCode);
        poll();
        TokenEntity token = ersteBankApiClient.getSidentityToken();
        ersteBankApiClient.saveToken(token);
    }

    private void poll() throws LoginException {
        for (int i = 0; i < ErsteBankConstants.SIDENTITY.MAX_SIDENTITY_POLLING_ATTEMPTS; i++) {
            PollResponse response;
            try {
                response = ersteBankApiClient.pollStatus();
            } catch (HttpResponseException hre) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }

            switch (response.getSecondFactorStatus()) {
                case ErsteBankConstants.SIDENTITY.POLL_DONE:
                    return;
                case ErsteBankConstants.SIDENTITY.POLL_WAITING:
                    break;
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "Unknown Sidentity status: %s",
                                    response.getSecondFactorStatus()));
            }
            Uninterruptibles.sleepUninterruptibly(
                    response.getPollingIntervalMs(), TimeUnit.MILLISECONDS);
        }
        throw LoginError.INCORRECT_CREDENTIALS.exception();
    }
}
