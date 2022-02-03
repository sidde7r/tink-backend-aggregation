package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ScaStatusValue;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

@RequiredArgsConstructor
@Slf4j
public class LansforsakringarDecoupledAuthenticator implements BankIdAuthenticator<String> {

    private final LansforsakringarApiClient apiClient;
    private final LansforsakringarStorageHelper storageHelper;
    private final Credentials credentials;
    private OAuth2Token accessToken;

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        OAuth2Token credentialsToken = generateCredentialsToken();
        createConsent(credentialsToken);
        ConsentResponse consentResponse = authorizeConsent(credentialsToken);
        return consentResponse.getLinks().getScaStatus().getHref();
    }

    private ConsentResponse authorizeConsent(OAuth2Token credentialsToken) {
        ConsentResponse consentResponse = apiClient.authorizeConsent(credentialsToken);
        storageHelper.setAuthorisationId(consentResponse.getAuthorisationId());
        return consentResponse;
    }

    private void createConsent(OAuth2Token credentialsToken) {
        ConsentResponse consent = apiClient.createConsent(credentialsToken);
        storageHelper.setConsentId(consent.getConsentId());
    }

    private OAuth2Token generateCredentialsToken() {
        OAuth2Token credentialsToken = apiClient.generateCredentialsToken();
        storageHelper.setOAuth2Token(credentialsToken);
        return credentialsToken;
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {

        ConsentResponse consentResponse = apiClient.getScaStatus(reference);

        switch (Strings.nullToEmpty(consentResponse.getScaStatus()).toLowerCase()) {
            case ScaStatusValue.RECEIVED:
            case ScaStatusValue.STARTED:
            case ScaStatusValue.EMPTY:
                return BankIdStatus.WAITING;

            case ScaStatusValue.FINALISED:
                accessToken =
                        apiClient.exchangeAuthorizationCode(consentResponse.getAuthorisationCode());
                storeAccessTokenAndSessionExpiryDate();
                return BankIdStatus.DONE;

            case ScaStatusValue.FAILED:
                return BankIdStatus.CANCELLED;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private void storeAccessTokenAndSessionExpiryDate() {
        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFromTokenOrDefault(
                        accessToken));
        storageHelper.setOAuth2Token(accessToken);
    }

    @Override
    public String refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        return String.valueOf(
                refreshAccessToken(accessToken.getRefreshToken())
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception));
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token newAccessToken =
                apiClient.refreshToken(
                        String.valueOf(
                                storageHelper
                                        .getOAuth2Token()
                                        .flatMap(OAuth2TokenBase::getOptionalRefreshToken)
                                        .orElseThrow(
                                                LoginError.CREDENTIALS_VERIFICATION_ERROR
                                                        ::exception)));
        accessToken = newAccessToken;
        return Optional.of(newAccessToken);
    }
}
