package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps;

import java.time.temporal.ChronoUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class GetTokensStep implements AuthenticationStep {
    private final SebBalticsBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public GetTokensStep(
            SebBalticsBaseApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        TokenResponse tokenResponse =
                apiClient.getDecoupledToken(
                        DecoupledTokenRequest.builder()
                                .grantType("decoupled_authorization")
                                .authorizationId(sessionStorage.get(StorageKeys.AUTH_REQ_ID))
                                .build());

        OAuth2Token oAuth2Token = tokenResponse.toTinkToken();

        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        oAuth2Token,
                        (int) tokenResponse.getRefreshTokenExpiresIn(),
                        ChronoUnit.SECONDS));

        //  String consentStatus =
        // apiClient.getConsentStatus(persistentStorage.getOptional(StorageKeys.USER_CONSENT_ID).orElse("")).getConsentStatus();

        //   if(consentStatus.equals("valid")){
        //     return AuthenticationStepResponse.authenticationSucceeded();
        //   } else {
        //     return AuthenticationStepResponse.executeNextStep();
        //   }
        return AuthenticationStepResponse.executeNextStep();
    }
}
