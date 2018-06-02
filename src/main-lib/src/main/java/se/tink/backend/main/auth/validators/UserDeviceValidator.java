package se.tink.backend.main.auth.validators;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import se.tink.libraries.endpoints.EndpointsConfiguration;
import se.tink.backend.core.TinkUserAgent;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.utils.CredentialsPredicate;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class UserDeviceValidator {

    private static final LogUtils log = new LogUtils(UserDeviceValidator.class);
    private static final String URL_MULTI_FACTOR_AUTHENTICATION = "{0}://{1}/oauth/authorize/?device_id={2}&authorization={3}#authorize-device";

    private final boolean isDevelopmentMode;
    private final CredentialsRepository credentialsRepository;

    private String host;
    private String protocol;

    @Inject
    public UserDeviceValidator(@Named("developmentMode") boolean isDevelopmentMode,
            EndpointsConfiguration endpointsConfiguration, CredentialsRepository credentialsRepository) {
        this.isDevelopmentMode = isDevelopmentMode;
        this.credentialsRepository = credentialsRepository;

        try {
            URL url = new URL(endpointsConfiguration.getAPI().getUrl());

            host = url.getHost();
            protocol = url.getProtocol();
        } catch (MalformedURLException e) {
            log.error("Could not parse host from API endpoint URL: " + endpointsConfiguration.getAPI(), e);
        }
    }

    public void validateDevice(User user, UserDevice device) {
        validateDevice(user, device, null, Optional.empty());
    }

    public void validateDevice(User user, UserDevice device, String authorizationValue, Optional<String> userAgent) {

        if (isDevelopmentMode) {
            // Don't use device pinning in development mode.
            return;
        }

        String market = user.getProfile().getMarket();
        if (!(Objects.equal(market, Market.Code.SE.name()) || Objects.equal(market, Market.Code.NL.name()))) {
            // no device pinning for other markets
            return;
        }


        if (userAgent.isPresent()) {
            TinkUserAgent tinkUserAgent = new TinkUserAgent(userAgent.get());
            if ((Objects.equal(market, Market.Code.NL.name())) && !tinkUserAgent.hasValidVersion("4.0.0","4.0.0")) {
                // old grip
                return;
            }
        }


        if (device != null) {
            if (device.getStatus() == UserDeviceStatuses.AUTHORIZED) {
                return;
            } else if (Objects.equal(market, Market.Code.SE.name()) && !userCredentialsHasSwedishSsn(user)) {
                // If none of the credentials has a Swedish ssn that means we cannot two-factor the user. Let them in.
                // CreditSafe / ID-koll also creates a credential, so we should be able to look at that one in first
                // place.
                return;
            }
        }

        // Non-existing DEVICE-ID header is only OK if it was an existing session.

        if (device == null) {
            throw new UnsupportedClientException(
                    Catalog.getCatalog(user.getProfile().getLocale())
                            .getString(
                                    "You're using a version of the app that is too old. Please update to the latest version in order to login to your Tink account."));
        }

        log.warn(user.getId(), "Unable to validate authorized device: " + device.getDeviceId());

        throw new UnauthorizedDeviceException(Catalog.format(URL_MULTI_FACTOR_AUTHENTICATION, protocol, host,
                device.getDeviceId(), authorizationValue == null ? "" : URLEncoder.encode(authorizationValue)));
    }

    private boolean userCredentialsHasSwedishSsn(User user) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());

        return credentials.stream().filter(Predicates.not(Predicates.IS_DEMO_CREDENTIALS)::apply)
                .anyMatch(CredentialsPredicate.CREDENTIAL_HAS_SSN_USERNAME);
    }
}
