package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class DnbAuthenticator implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final DnbApiClient apiClient;
    private final Credentials credentials;

    public DnbAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            DnbApiClient apiClient,
            Credentials credentials) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public void autoAuthenticate() {
        if (!apiClient.isConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        if (StringUtils.isEmpty(credentials.getField(DnbConstants.CredentialsKeys.PSU_ID))) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        } else {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        final URL authorizeUrl = apiClient.getAuthorizeUrl(strongAuthenticationState.getState());
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(final String reference) {
        Optional<Map<String, String>> supplementalInfo =
                this.supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        ThirdPartyAppStatus result;
        if (!supplementalInfo.isPresent()) {
            result = ThirdPartyAppStatus.TIMED_OUT;
        } else if (apiClient.isConsentValid()) {
            result = ThirdPartyAppStatus.DONE;
        } else {
            result = ThirdPartyAppStatus.AUTHENTICATION_ERROR;
        }
        return ThirdPartyAppResponseImpl.create(result);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(final ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
