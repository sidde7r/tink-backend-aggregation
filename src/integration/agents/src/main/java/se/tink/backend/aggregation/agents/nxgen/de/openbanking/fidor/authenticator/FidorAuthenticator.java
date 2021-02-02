package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.FieldKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.ConsentRedirectResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;

public class FidorAuthenticator
        implements Authenticator, ThirdPartyAppAuthenticator<String>, AutoAuthenticator {

    private final StrongAuthenticationState strongAuthenticationState;
    private SupplementalInformationHelper supplementalInformationHelper;
    private final FidorApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private Credentials credentials;
    private ConsentRedirectResponse consentResponse;

    public FidorAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            FidorApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public void authenticate(Credentials credentials) {

        String username = credentials.getField(FieldKeys.USERNAME);
        String password = credentials.getField(FieldKeys.PASSWORD);
        String iban = credentials.getField(FieldKeys.IBAN);
        String bban = credentials.getField(FieldKeys.BBAN);
        OAuth2Token token = apiClient.getToken(username, password).toTinkToken();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
        consentResponse = apiClient.getConsent(iban, bban);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
    }

    public URL getScaRedirectUrl() {
        return new URL(consentResponse.getLinks().getScaRedirect().getHref());
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(final String reference) {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        authenticate(credentials);
        return ThirdPartyAppAuthenticationPayload.of(getScaRedirectUrl());
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(final ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
