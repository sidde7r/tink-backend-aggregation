package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.decoupled.authenticator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class DecoupledThirdPartyAppAuthenticator implements ThirdPartyAppAuthenticator {
    private enum FailureCause {
        IN_PROGRESS,
        UNKNOWN,
        TIMEOUT,
        CANCELLED
    }

    private static final String DEMO_USERNAME = "tink-demo-user";
    private static final int TOTAL_ATTEMPTS = 5;

    private final Credentials credentials;
    private final String username;
    private final boolean successfulAuthentication;
    private final FailureCause failureCause;

    private int attempt = 0;

    private DecoupledThirdPartyAppAuthenticator(
            Credentials credentials, String username, boolean successfulAuthentication) {
        this(credentials, username, successfulAuthentication, null);
    }

    private DecoupledThirdPartyAppAuthenticator(
            Credentials credentials,
            String username,
            boolean successfulAuthentication,
            FailureCause failureCause) {
        this.credentials = credentials;
        this.username = username;
        this.successfulAuthentication = successfulAuthentication;
        this.failureCause = failureCause;
    }

    public static DecoupledThirdPartyAppAuthenticator createSuccessfulAuthenticator(
            Credentials credentials, String username) {
        return new DecoupledThirdPartyAppAuthenticator(credentials, username, true);
    }

    @Override
    public ThirdPartyAppResponse init() {
        if (!DEMO_USERNAME.equals(username)) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.CANCELLED);
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse collect(Object reference)
            throws AuthenticationException, AuthorizationException {
        if (attempt <= TOTAL_ATTEMPTS) {
            attempt++;
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
        }

        if (successfulAuthentication || Objects.isNull(failureCause)) {
            Date sessionExpiryDate =
                    Date.from(
                            LocalDateTime.now()
                                    .plusDays(90)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant());

            credentials.setSessionExpiryDate(sessionExpiryDate);

            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
        }

        switch (failureCause) {
            case IN_PROGRESS:
                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.ALREADY_IN_PROGRESS);
            case TIMEOUT:
                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.TIMED_OUT);
            case CANCELLED:
                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.CANCELLED);
            case UNKNOWN:
                // intentional fall through
            default:
                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.UNKNOWN);
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        payload.setDownloadTitle("Download Tink Demo Authentication app");
        payload.setDownloadMessage(
                "You need to download the Tink Demo Authentication app in order to continue.");

        payload.setUpgradeTitle("Upgrade Tink Demo Authentication app");
        payload.setUpgradeMessage(
                "You need to upgrade the Tink Demo Authentication app in order to continue.");

        ThirdPartyAppAuthenticationPayload.Ios ios = new ThirdPartyAppAuthenticationPayload.Ios();
        ios.setAppScheme("this.is.not.a.valid.app.scheme");
        ios.setDeepLinkUrl("this.is.not.a.valid.deeplink");
        ios.setAppStoreUrl("https://itunes.apple.com");

        payload.setIos(ios);

        ThirdPartyAppAuthenticationPayload.Android android =
                new ThirdPartyAppAuthenticationPayload.Android();
        android.setIntent("this.is.not.a.valid.intent");
        android.setPackageName("this.is.not.a.valid.package.name");
        android.setRequiredVersion(0);

        payload.setAndroid(android);

        ThirdPartyAppAuthenticationPayload.Desktop desktop =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        desktop.setUrl("https://test.com");
        payload.setDesktop(desktop);

        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
