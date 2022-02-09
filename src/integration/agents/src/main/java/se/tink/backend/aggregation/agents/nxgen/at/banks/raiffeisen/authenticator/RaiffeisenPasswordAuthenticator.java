package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenWebApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class RaiffeisenPasswordAuthenticator implements PasswordAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(RaiffeisenPasswordAuthenticator.class);
    private final RaiffeisenWebApiClient apiClient;
    private final RaiffeisenSessionStorage sessionStorage;
    private final Credentials credentials;

    public RaiffeisenPasswordAuthenticator(
            final Credentials credentials,
            final RaiffeisenWebApiClient apiClient,
            final RaiffeisenSessionStorage sessionStorage) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private static boolean isWrongPin(HttpResponse passwordResponse) {
        return passwordResponse.getBody(String.class).toLowerCase().contains("die pin ist falsch");
    }

    private static URL getSsoUrl(HttpResponse radSessionIdResponse) {
        InputStream is = radSessionIdResponse.getBodyInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        try {
            while (br.ready()) {
                final String line = br.readLine();
                final int startIdx = line.indexOf(RaiffeisenConstants.Url.SSO_BASE.toString());
                if (startIdx != -1) {
                    final int endIdxSingleQuote = line.indexOf('\'', startIdx);
                    final int endIdxDoubleQuote = line.indexOf('"', startIdx);
                    final int min = Integer.min(endIdxSingleQuote, endIdxDoubleQuote);
                    final int max = Integer.max(endIdxSingleQuote, endIdxDoubleQuote);
                    if (max == -1) {
                        continue;
                    } else if (min == -1) {
                        return new URL(line.substring(startIdx, max));
                    } else {
                        return new URL(line.substring(startIdx, min));
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException("Failed to find SSO URL");
    }

    private static String getToken(URL url, Pattern pattern) {
        String s = url.toUri().getFragment();
        Matcher m = pattern.matcher(s);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalStateException("Failed to extract token from URL: " + url);
    }

    private static String getAccessToken(URL url) {
        return getToken(url, Pattern.compile(RaiffeisenConstants.RegExpPatterns.ACCESS_TOKEN));
    }

    private static String getTokenType(URL url) {
        return getToken(url, Pattern.compile(RaiffeisenConstants.RegExpPatterns.TOKEN_TYPE));
    }

    private MessageDigest getMD5() {
        // Every implementation of the Java platform is required to support the MD5 MessageDigest
        // algorithm...
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String getEncryptedPassword(final String plaintextPassword) {
        final MessageDigest md = getMD5();
        md.update(plaintextPassword.getBytes(StandardCharsets.UTF_8));
        final byte[] digest = md.digest();
        return Hex.encodeHexString(digest).toLowerCase(Locale.US);
    }

    private void checkUsername(final String username) throws LoginException {
        final int usernameLength = RaiffeisenConstants.IntValues.USERNAME_LENGTH;
        if (username.length() != usernameLength) {
            logger.warn(
                    "The signatory number has {} characters; expected {} characters",
                    username.length(),
                    usernameLength);
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    new LocalizableKey(
                            String.format(
                                    "The signatory number has %d characters; expected %d characters",
                                    username.length(), usernameLength)));
        }
    }

    private void checkPassword(final String password) throws LoginException {
        final int passwordLength = RaiffeisenConstants.IntValues.PASSWORD_LENGTH;
        if (password.length() != passwordLength) {
            logger.warn(
                    "The PIN has {} characters; expected {} digits",
                    password.length(),
                    passwordLength);
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    new LocalizableKey(
                            String.format(
                                    "The PIN has %d characters; expected %d digits",
                                    password.length(), passwordLength)));
        }
    }

    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException {
        checkUsername(username);
        checkPassword(password);
        final String encryptedPassword = getEncryptedPassword(password);
        credentials.setSensitivePayload(Key.PASSWORD, encryptedPassword);
        final HttpResponse homeResponse = apiClient.getHomePage();
        final HttpResponse refreshRegionResponse = apiClient.RefreshRegion(homeResponse);
        final HttpResponse usernameResponse =
                apiClient.sendUsername(refreshRegionResponse, username);
        final HttpResponse passwordResponse =
                apiClient.sendPassword(usernameResponse, encryptedPassword);
        if (isWrongPin(passwordResponse)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        final HttpResponse selectionResponse = apiClient.sendSelection(passwordResponse);
        final HttpResponse radSessionIdResponse = apiClient.sendRadSessionId(selectionResponse);
        final URL ssoUrl = getSsoUrl(radSessionIdResponse);
        apiClient.sendSsoRequest(ssoUrl);
        final URL url = apiClient.sso();
        final WebLoginResponse webLoginResponse =
                new WebLoginResponse(getTokenType(url), getAccessToken(url));
        sessionStorage.setWebLoginResponse(webLoginResponse);
    }
}
