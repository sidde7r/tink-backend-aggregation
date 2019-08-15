package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;

public final class FinecoBankAuthenticator
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Random random = new SecureRandom();
    private static final long WAIT_FOR_MINUTES = 9L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;
    private final FinecoBankAuthenticationHelper finecoAuthenticator;
    private final String state;
    private static final Encoder encoder = Base64.getUrlEncoder();

    public FinecoBankAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage,
            FinecoBankAuthenticationHelper finecoAuthenticator) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
        this.finecoAuthenticator = finecoAuthenticator;
        this.state = generateRandomState();
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {

        if (Strings.isNullOrEmpty(persistentStorage.get(StorageKeys.CONSENT_ID))
                || !finecoAuthenticator.getApprovedConsent()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {
        long startTime = System.currentTimeMillis();
        this.supplementalInformationHelper
                .waitForSupplementalInformation(
                        this.formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES)
                .orElseThrow(
                        () ->
                                new IllegalMonitorStateException(
                                        "No supplemental info found in api response"));

        while (!finecoAuthenticator.getApprovedConsent()
                || System.currentTimeMillis() - startTime < FormValues.MAX_TIMEOUT_COUNTER) {
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

        finecoAuthenticator.storeAccounts();
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        return ThirdPartyAppAuthenticationPayload.of(finecoAuthenticator.buildAuthorizeUrl(state));
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private String formatSupplementalKey(final String key) {
        return String.format(QueryKeys.SUPPLEMENTAL_INFORMATION, key);
    }

    private static String generateRandomState() {
        final byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }
}
