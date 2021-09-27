package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.AuthenticationMethod;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.AuthorisationStatus;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.PollValues;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.LhvAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.AuthorisationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.enums.MarketCode;

@RequiredArgsConstructor
public class ScaStep implements AuthenticationStep {

    private final LhvApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final LhvAuthenticator authenticator;

    public final AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final Credentials credentials = request.getCredentials();
        final String psuId = verifyCredentialsIsNotNullOrEmpty(credentials.getField(Key.USERNAME));

        final String psuCorporateId =
                verifyCredentialsIsNotNullOrEmpty(credentials.getField(Key.CORPORATE_ID));

        request.getCredentials()
                .setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getDefaultExpirationDate());

        final AuthorisationResponse authorisationResponse =
                apiClient.login(
                        new LoginRequest(AuthenticationMethod.SMART_ID, QueryValues.NULL),
                        psuId,
                        MarketCode.EE + psuCorporateId);

        sessionStorage.put(
                StorageKeys.AUTHORISATION_ID, authorisationResponse.getAuthorisationId());

        authenticator.displayChallengeCodeToUser(
                authorisationResponse.getChallengeData().getData());
        poll(authorisationResponse.getAuthorisationId());

        return AuthenticationStepResponse.executeNextStep();
    }

    public String verifyCredentialsIsNotNullOrEmpty(String credentials) throws LoginException {
        if (Strings.isNullOrEmpty(credentials) || credentials.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return credentials;
    }

    private void poll(String authorisationId)
            throws AuthenticationException, AuthorizationException {
        AuthorisationStatusResponse authorisationStatusResponse;

        for (int i = 0; i < PollValues.SMART_ID_POLL_MAX_ATTEMPTS; i++) {
            authorisationStatusResponse = apiClient.checkAuthorisationStatus(authorisationId);
            switch (authorisationStatusResponse.getScaStatus()) {
                case AuthorisationStatus.FINALISED:
                    sessionStorage.put(
                            StorageKeys.AUTHORISATION_CODE,
                            authorisationStatusResponse.getAuthorisationCode());
                    return;
                case AuthorisationStatus.STARTED:
                    break;
                case AuthorisationStatus.SCA_METHOD_SELECTED:
                    break;
                case AuthorisationStatus.RECEIVED:
                    break;
                default:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    PollValues.SMART_ID_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        throw ThirdPartyAppError.TIMED_OUT.exception();
    }
}
