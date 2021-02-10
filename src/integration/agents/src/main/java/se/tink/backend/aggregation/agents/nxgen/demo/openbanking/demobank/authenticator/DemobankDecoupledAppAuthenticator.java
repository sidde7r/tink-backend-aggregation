package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CollectTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class DemobankDecoupledAppAuthenticator implements TypedAuthenticator, Authenticator {

    private final DemobankApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    public DemobankDecoupledAppAuthenticator(
            DemobankApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        CreateTicketResponse response =
                this.apiClient.initDecoupledAppToApp(
                        credentials.getField(Key.USERNAME), credentials.getField("code"));
        displayVerificationCode();
        OAuth2Token token = awaitConfirmation(response.getTicket());

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        token,
                        DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME,
                        DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME_UNIT));
        apiClient.setTokenToStorage(token);
    }

    private OAuth2Token awaitConfirmation(String ticket) throws LoginException {
        try {
            CollectTicketResponse response =
                    RetryerBuilder.<CollectTicketResponse>newBuilder()
                            .retryIfResult(
                                    result ->
                                            result == null || "PENDING".equals(result.getStatus()))
                            .withWaitStrategy(WaitStrategies.fixedWait(3, TimeUnit.SECONDS))
                            .withStopStrategy(StopStrategies.stopAfterAttempt(60))
                            .build()
                            .call(() -> apiClient.collectAppToApp(ticket));
            if ("CONFIRMED".equals(response.getStatus())) {
                return response.getOAuthToken();

            } else {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
        } catch (ExecutionException | RetryException e) {
            throw new IllegalStateException("Exception thrown getting auth ticket", e);
        }
    }

    private void displayVerificationCode() {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description("Status")
                        .value("Waiting for bank consent")
                        .name("name")
                        .helpText("Please confirm the login in the Demobank Authenticator app.")
                        .build();

        supplementalInformationController.askSupplementalInformationAsync(field);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
