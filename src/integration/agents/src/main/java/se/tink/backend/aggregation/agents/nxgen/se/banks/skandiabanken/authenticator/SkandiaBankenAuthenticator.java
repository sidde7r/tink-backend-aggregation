package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.InitTokenResponse;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SkandiaBankenAuthenticator implements BankIdAuthenticator<String> {
    private final SkandiaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;
    private String autoStartToken;
    private OAuth2Token oAuth2Token;

    public SkandiaBankenAuthenticator(
            SkandiaBankenApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn) throws BankServiceException {
        fetchInitAccessToken();
        createSession();
        final String token = extractRequestVerificationToken();
        Preconditions.checkNotNull(token);
        final String url = apiClient.autoStartAuthenticate(token).getAutoStartUrl();
        setAutoStartToken(url);

        return extractRequestVerificationToken2();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        final BankIdResponse bankIdResponse = apiClient.collectBankId(reference);
        final String redirectUrl = bankIdResponse.getRedirectUrl();

        if (redirectUrl.equalsIgnoreCase(Endpoints.MESSAGE)) {
            String message = apiClient.fetchMessage();
            if (isNotCustomerMessage(message)) {
                throw LoginError.NOT_CUSTOMER.exception();
            } else {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
        }
        if (redirectUrl.equalsIgnoreCase(Endpoints.OTP_CHOOSER)) {
            apiClient.fetchOtpChooser();
            throw LoginError.NOT_SUPPORTED.exception();
        }

        if (!Strings.isNullOrEmpty(redirectUrl)) {
            final String code = fetchCode(redirectUrl);
            fetchBearerToken(code);
            return BankIdStatus.DONE;
        }

        if (bankIdResponse.getState() == 4 && !bankIdResponse.getMessage().isContinueCollect()) {
            if (bankIdResponse.getMessage().isShowInstallBankIdButton()) {
                return BankIdStatus.NO_CLIENT;
            } else if (bankIdResponse.getMessage().isShowRetryButton()) {
                return BankIdStatus.TIMEOUT;
            } else if (bankIdResponse.getMessage().getHeader().contains("avbryta inloggningen")) {
                return BankIdStatus.CANCELLED;
            }
            return BankIdStatus.FAILED_UNKNOWN;
        }

        return BankIdStatus.WAITING;
    }

    private boolean isNotCustomerMessage(String message) {
        if (Strings.isNullOrEmpty(message)) {
            return false;
        }

        if (message.toLowerCase().contains(SkandiaBankenConstants.ErrorMessages.NOT_CUSTOMER)) {
            return true;
        }

        return false;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    private void fetchInitAccessToken() {
        final InitTokenResponse initTokenResponse = apiClient.fetchInitAccessToken();
        final String initAccessToken =
                String.format("Bearer %s", initTokenResponse.getAccessToken());
        sessionStorage.put(StorageKeys.INIT_ACCESS_TOKEN, initAccessToken);
    }

    private void createSession() {
        final CreateSessionRequest sessionRequest =
                new CreateSessionRequest(
                        Authentication.REGISTRATION_TOKEN, Authentication.INSTANCE_ID);
        apiClient.createSession(sessionRequest);
    }

    private String extractRequestVerificationToken() {
        final String codeVerifier = generateCodeVerifier();
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        final String tokenResponse = apiClient.extractRequestVerificationToken(codeVerifier);

        String requestVerificationToken =
                Jsoup.parse(tokenResponse)
                        .getElementById("autostart-page")
                        .getElementsByAttributeValue("name", "__RequestVerificationToken")
                        .attr("value");

        // store it in session storage so it will masked in logs
        sessionStorage.put(StorageKeys.REQUEST_VER_TOKEN, requestVerificationToken);
        return requestVerificationToken;
    }

    private String extractRequestVerificationToken2() {
        final String tokenResponse = apiClient.autoStartAuthenticateRedirect();

        return Jsoup.parse(tokenResponse)
                .getElementById("collect-page")
                .getElementsByAttributeValue("name", "__RequestVerificationToken")
                .attr("value");
    }

    private void setAutoStartToken(String url) {
        // extract auto start token
        final Pattern autoStartTokenPattern = Pattern.compile("=(.*)&");
        Matcher autoStartTokenMatcher = autoStartTokenPattern.matcher(url);
        if (!autoStartTokenMatcher.find()) {
            throw new IllegalArgumentException("Could not parse auto start token from " + url);
        }
        autoStartToken = autoStartTokenMatcher.group(1);
    }

    private String fetchCode(String redirect) {
        final String deepLink = apiClient.fetchCode(redirect).getHeaders().get("Location").get(0);
        final Pattern codePattern = Pattern.compile("=(.*)&");
        Matcher codeMatcher = codePattern.matcher(deepLink);
        if (!codeMatcher.find()) {
            throw new IllegalArgumentException("Could not parse code from " + deepLink);
        }
        return codeMatcher.group(1);
    }

    private void fetchBearerToken(String code) {
        oAuth2Token = apiClient.fetchAuthToken(code).toOAuth2Token();
    }

    private String generateCodeVerifier() {
        final int len = 64;

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final int index = RandomUtils.randomInt(len);
            final char randomCharacter =
                    SkandiaBankenConstants.Authentication.CODE_VERIFIER_CHARSET.charAt(index);
            sb.append(randomCharacter);
        }
        return sb.toString();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(oAuth2Token);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.ofNullable(apiClient.refreshToken(refreshToken).toOAuth2Token());
    }
}
