package se.tink.backend.aggregation.nxgen.controllers.utils.sca;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class ScaRedirectPollHandler<E extends Throwable> {
    final SupplementalInformationHelper supplementalInformationHelper;
    final int attempts;
    final long waitBetweenAttempts;
    final TimeUnit unit;

    /**
     * @param supplementalInformationHelper
     * @param attempts number of times to poll
     * @param waitBetweenAttempts time to wait between polling
     * @param unit time to wait between polling
     */
    public ScaRedirectPollHandler(
            SupplementalInformationHelper supplementalInformationHelper,
            int attempts,
            long waitBetweenAttempts,
            TimeUnit unit) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.attempts = attempts;
        this.waitBetweenAttempts = waitBetweenAttempts;
        this.unit = unit;
    }

    /**
     * Poll for a response. On error, this method should throw an appropriate exception.
     *
     * @return true on success, false to continue polling
     * @throws E fail with an exception
     */
    protected abstract boolean poll() throws E;

    private ThirdPartyAppAuthenticationPayload getRedirectPayload(URL url) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(url.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(url.getScheme());
        iOsPayload.setDeepLinkUrl(url.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    /**
     * Handle a SCA redirect by opening the URL in the client, and polling for a response.
     *
     * @param redirectUrl URL to open on the client
     * @return true on success, false on timeout
     */
    public boolean handleRedirect(URL redirectUrl) throws E {
        final ThirdPartyAppAuthenticationPayload payload = getRedirectPayload(redirectUrl);
        supplementalInformationHelper.openThirdPartyApp(payload);

        for (int i = 0; i < attempts; i++) {
            if (poll()) {
                return true;
            } else {
                Uninterruptibles.sleepUninterruptibly(waitBetweenAttempts, unit);
            }
        }

        return false;
    }
}
