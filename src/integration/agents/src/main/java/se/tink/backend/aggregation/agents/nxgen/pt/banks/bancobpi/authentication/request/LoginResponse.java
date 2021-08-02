package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class LoginResponse extends AuthenticationResponse {

    private static final String CSRF_TOKEN_COOKIE = "nr2LB_BPIParticulares";
    private static final Pattern CSRF_TOKEN_PATTERN = Pattern.compile("crf\\=(.+)\\;uid");

    private String csrfToken;

    LoginResponse(final HttpResponse response) throws RequestException {
        super(response.getBody(String.class));
        csrfToken = extractCSRFTokenFromCookies(response.getCookies());
    }

    String extractCSRFTokenFromCookies(final List<NewCookie> cookies) throws RequestException {
        String cookieValue =
                cookies.stream()
                        .filter(c -> c.getName().equals(CSRF_TOKEN_COOKIE))
                        .map(Cookie::getValue)
                        .findAny()
                        .orElseThrow(() -> new RequestException("CSRF token cookie not found"));
        try {
            Matcher m =
                    CSRF_TOKEN_PATTERN.matcher(
                            URLDecoder.decode(cookieValue, StandardCharsets.UTF_8.toString()));
            if (!m.find()) {
                throw new RequestException("CSRF token not found in cookies");
            }
            return m.group(1);
        } catch (UnsupportedEncodingException e) {
            throw new RequestException("Unexpected CSRF token cookie value format");
        }
    }

    public String getCsrfToken() {
        return csrfToken;
    }
}
