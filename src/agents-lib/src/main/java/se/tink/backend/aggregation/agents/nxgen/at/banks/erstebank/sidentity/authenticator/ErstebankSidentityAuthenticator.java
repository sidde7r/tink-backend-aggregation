package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.rpc.PollResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;

public class ErstebankSidentityAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final ErsteBankApiClient ersteBankApiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public ErstebankSidentityAuthenticator(ErsteBankApiClient ersteBankApiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.ersteBankApiClient = ersteBankApiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        // Fix for not refreshing the credential automatically
        return CredentialsTypes.MOBILE_BANKID;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String username = credentials.getUsername();
        String verificationCode = ersteBankApiClient.getSidentityVerificationCode(username);
        supplementalInformationHelper.waitAndShowLoginDescription(verificationCode);
        poll();
        TokenEntity token = ersteBankApiClient.getSidentityToken();
        ersteBankApiClient.saveToken(token);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException, AuthorizationException {

        if (ersteBankApiClient.tokenExists() &&
                ersteBankApiClient.getTokenFromStorage().getExpiryDate().before(new Date())) {
            return;
        }

        throw SessionError.SESSION_EXPIRED.exception();
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
                        String.format("Unknown Sidentity status: %s", response.getSecondFactorStatus()));
            }
            Uninterruptibles.sleepUninterruptibly(response.getPollingIntervalMs(), TimeUnit.MILLISECONDS);
        }
        throw LoginError.INCORRECT_CREDENTIALS.exception();
    }
}
